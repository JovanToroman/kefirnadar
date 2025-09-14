(ns kefirnadar.application.auth
  (:require [cuerdas.core :as str]
            [kefirnadar.application.specs :as specs]
            [applied-science.js-interop :as j]
            [kefirnadar.application.utils.route :as route-utils]
            [kefirnadar.application.validation :as validation]
            [re-frame.core :refer [reg-event-db reg-event-fx trim-v reg-sub dispatch]]
            [reagent.cookies :as cookies]
            [kefirnadar.application.fx :as fx]
            [taoensso.timbre :as log]))

;; region Kolacici
;; TODO: how to make this safer, i.e. protect against identity theft
(def user-cookie-name "1e44b7d6cbff2e8a650a43d3a21b8e2e")

(defn set-user-cookie-info!
  [user-info]
  (let [date (js/Date.)]
    (j/call date :setDate (+ (j/call date :getDate) 30))
    (j/assoc! js/document :cookie (str/format "%s=%s;path=/;expires=%s" user-cookie-name (pr-str user-info) date))))

(defn get-user-cookie-info []
  (cookies/get user-cookie-name))

(defn remove-user-cookie-info! []
  (cookies/remove! user-cookie-name))
;; endregion

(defprotocol AuthMethod
  (get-login-status [this] "Checks whether the current user is logged in with this auth method")
  (handle-login-response [this response] "Finish logging the user in if they were authenticated by the auth provider")
  (log-user-in [this] "Logs the user in using the auth method")
  (log-user-out [this] "Logs the user out using the auth method")
  (dodaj-korisnika [this podaci-korisnika] "Dodaje novog korisnika"))

(defrecord FacebookLogin [] AuthMethod
  (get-login-status [this]
    (j/call js/FB :getLoginStatus #(handle-login-response this %)))
  (handle-login-response [_this response]
    (let [{:keys [status authResponse]}
          ^::specs/facebook-get-login-status-response (update (js->clj response :keywordize-keys true) :status keyword)
          authenticated? (case status
                           :connected true
                           :unknown false
                           false)
          user-info (js->clj authResponse :keywordize-keys true)]
      (when authenticated?
        (j/call js/FB :api "/me" (fn [response]
                                   (let [{:keys [name]} (js->clj response :keywordize-keys true)
                                         user-info (assoc user-info :name name)]
                                     (set-user-cookie-info! {:status status
                                                             :authenticated? authenticated?
                                                             :method :facebook})
                                     (dispatch [::potvrdi-fejsbuk-korisnika user-info])))))))
  (log-user-in [this]
    (j/call js/FB :login #(handle-login-response this %) #js {:scope "public_profile"}))
  (log-user-out [_this]
    ;; we need to call :getLoginStatus because logout will not work if cache is cleared before invoking it.
    (j/call js/FB :getLoginStatus (fn [_]
                                    (j/call js/FB
                                      :logout #(dispatch [::obradi-odjavu-korisnika]))))))

(def auth-methods {:facebook (->FacebookLogin)})

;; region events
(reg-event-fx ::potvrdi-fejsbuk-korisnika trim-v
  (fn [_ [^::specs/authResponse user-info]]
    {::fx/api {:uri (route-utils/url-for "/api/auth/potvrdi-fejsbuk-korisnika")
               :method :post
               :params user-info
               :on-success [::ensure-user-success]
               :on-error [::ensure-user-fail]}}))

(reg-event-fx ::ensure-user-success trim-v
  (fn [_ [{:keys [korisnik]}]]
    (let [user-cookie (get-user-cookie-info)
          auth-data (assoc user-cookie :korisnik korisnik)]
      (set-user-cookie-info! auth-data)
      {:dispatch [::set-auth-data auth-data]
       :kefirnadar.application.events/load-route! {:data {:name :route/home}}})))

(reg-event-db ::ensure-user-fail trim-v (fn [db _] db))

(reg-event-db ::log-user-in trim-v
  (fn [_db [authentication-provider]]
    (log-user-in (authentication-provider auth-methods))))

(reg-event-db ::set-auth-data trim-v
  (fn [db [auth-data]]
    (assoc db :auth auth-data)))

(reg-event-db ::unset-auth-data trim-v
  (fn [db _]
    (dissoc db :auth)))

(reg-event-fx ::ucitaj-korisnika trim-v
  (fn [_ _]
    (when-some [cookie (get-user-cookie-info)]
      {:dispatch [::set-auth-data cookie]})))

(reg-event-fx ::dodaj-korisnika trim-v
  (fn [{:keys [db]} [^::specs/podaci-korisnika podaci-korisnika]]
    (let [validation-info (validation/validate-form-info podaci-korisnika)]
      (if (validation/forma-validna? validation-info)
        {::fx/api {:uri (route-utils/url-for "/api/auth/dodaj-korisnika")
                   :method :post
                   :params podaci-korisnika
                   :on-success [::dodaj-korisnika-uspeh]
                   :on-error [::dodaj-korisnika-neuspeh]}}
        {:db (assoc-in db [:ads :registracija :form-data-validation] validation-info)}))))

(reg-event-fx ::dodaj-korisnika-uspeh trim-v
  (fn [_ _]
    {:kefirnadar.application.events/load-route! {:data {:name :route/nakon-registracije}}}))

(reg-event-db ::dodaj-korisnika-neuspeh trim-v
  (fn [db [{:keys [greska]}]]
    (log/error "Greška pri registraciji korisnika")
    (assoc-in db [:ads :registracija :greska] greska)))

(reg-event-fx ::posalji-aktivacioni-kod trim-v
  (fn [_ [imejl]]
    {::fx/api {:uri (route-utils/url-for "/api/auth/posalji-aktivacioni-kod")
               :method :post
               :params {:imejl imejl}
               :on-success [::posalji-aktivacioni-kod-uspeh]
               :on-error [::posalji-aktivacioni-kod-neuspeh]}}))

(reg-event-fx ::odjavi-korisnika trim-v
  (fn [{:keys [db]} _]
    (let [method (get-in db [:auth :method])]
      (remove-user-cookie-info!)
      (when (= method :facebook)
        (log-user-out (:facebook auth-methods)))
      {:dispatch [::obradi-odjavu-korisnika]})))

(reg-event-fx ::obradi-odjavu-korisnika trim-v
  (fn [_ _]
    {:dispatch [::unset-auth-data]
     :kefirnadar.application.events/load-route! {:data {:name :route/home}}}))

(reg-event-fx ::posalji-aktivacioni-kod-uspeh trim-v
  (fn [db _]
    (assoc-in db [:auth :aktivacioni-kod-poslat?] true)))

(reg-event-db ::posalji-aktivacioni-kod-neuspeh trim-v
  (fn [db _]
    (log/error "Greška pri slanju aktivacionog koda")
    db))

(reg-event-fx ::aktiviraj-korisnika trim-v
  (fn [{:keys [_db]} [aktivacioni-kod]]
    {::fx/api {:uri (route-utils/url-for "/api/auth/aktiviraj-korisnika")
               :method :post
               :params {:aktivacioni-kod aktivacioni-kod}
               :on-success [::aktiviraj-korisnika-uspeh]
               :on-error [::aktiviraj-korisnika-neuspeh]}}))

(reg-event-fx ::aktiviraj-korisnika-uspeh trim-v
  (fn [_ _]
    {:kefirnadar.application.events/load-route! {:data {:name :route/prijava}}}))

(reg-event-db ::aktiviraj-korisnika-neuspeh trim-v
  (fn [db [{:keys [greska]}]]
    (log/error "Greška pri aktiviranju korisnika")
    (assoc-in db [:ads :aktiviraj-korisnika :greska] greska)))

(reg-event-fx ::prijava trim-v
  (fn [{:keys [db]} [^::specs/podaci-korisnika podaci-korisnika js-event]]
    (j/call js-event :preventDefault)
    (let [validation-info (validation/validate-form-info podaci-korisnika)]
      (if (validation/forma-validna? validation-info)
        {::fx/api {:uri (route-utils/url-for "/api/auth/prijava")
                   :method :post
                   :params podaci-korisnika
                   :on-success [::prijava-uspeh]
                   :on-error [::prijava-neuspeh]}
         :db (assoc-in db [:ads :prijava :form-data-validation] {})}
        {:db (assoc-in db [:ads :prijava :form-data-validation] validation-info)}))))

(reg-event-fx ::prijava-uspeh trim-v
  (fn [_ [{:keys [korisnik]}]]
    (let [user-cookie (get-user-cookie-info)
          auth-data (merge user-cookie {:authenticated? true
                                        :method :obican
                                        :korisnik korisnik})]
      (set-user-cookie-info! auth-data)
      {:dispatch [::set-auth-data auth-data]
       :kefirnadar.application.events/load-route! {:data {:name :route/home}}})))

(reg-event-db ::prijava-neuspeh trim-v
  (fn [db [{:keys [greska]}]]
    (log/error "Greška pri prijavi korisnika")
    (assoc-in db [:ads :prijava :greska] greska)))

(reg-event-fx ::posalji-imejl-za-resetovanje-lozinke trim-v
  (fn [_ [imejl]]
    {::fx/api {:uri (route-utils/url-for "/api/auth/posalji-imejl-za-resetovanje-lozinke")
               :method :post
               :params {:imejl imejl}
               :on-success [::posalji-imejl-za-resetovanje-lozinke-uspeh]
               :on-error [::posalji-imejl-za-resetovanje-lozinke-neuspeh]}}))

(reg-event-fx ::posalji-imejl-za-resetovanje-lozinke-uspeh trim-v
  (fn [_ _]
    {:kefirnadar.application.events/load-route! {:data {:name :route/nakon-slanja-imejla-za-resetovanje-lozinke}}}))

(reg-event-db ::posalji-imejl-za-resetovanje-lozinke-neuspeh trim-v
  (fn [db _]
    (assoc-in db [:ads :slanje-imejla-za-resetovanje-lozinke :form-data-validation :imejl] false)))

(reg-event-fx ::resetuj-lozinku trim-v
  (fn [{:keys [db]} [kod-za-resetovanje-lozinke lozinka]]
    (let [validation-info (validation/validate-form-info lozinka)]
      (if (validation/forma-validna? validation-info)
        {::fx/api {:uri (route-utils/url-for "/api/auth/resetuj-lozinku")
                   :method :post
                   :params (assoc lozinka :kod-za-resetovanje-lozinke kod-za-resetovanje-lozinke)
                   :on-success [::resetuj-lozinku-uspeh]
                   :on-error [::resetuj-lozinku-neuspeh]}
         :db (assoc-in db [:ads :resetovanje-lozinke :form-data-validation] {})}
        {:db (assoc-in db [:ads :resetovanje-lozinke :form-data-validation] validation-info)}))))

(reg-event-fx ::resetuj-lozinku-uspeh trim-v
  (fn [_ _]
    {:kefirnadar.application.events/load-route! {:data {:name :route/nakon-resetovanja-lozinke}}}))

(reg-event-db ::resetuj-lozinku-neuspeh trim-v
  (fn [db [{:keys [greska]}]]
    (log/error "Greška pri resetovanju lozinke")
    (assoc-in db [:ads :resetovanje-lozinke :greska] greska)))
;; endregion

;; region subs
(reg-sub ::authenticated?
  (fn [db _]
    (get-in db [:auth :authenticated?])))

(reg-sub ::user-info
  (fn [db _]
    (get-in db [:auth :korisnik])))

(reg-sub ::user-id :<- [::user-info]
  (fn [user-info _]
    (:id-korisnika user-info)))
;; endregion