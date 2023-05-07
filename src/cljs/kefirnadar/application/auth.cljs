(ns kefirnadar.application.auth
  (:require [kefirnadar.application.specs :as specs]
            [applied-science.js-interop :as j]
            [kefirnadar.application.utils.route :as route-utils]
            [re-frame.core :refer [reg-event-db reg-event-fx trim-v reg-sub dispatch]]
            [reagent.cookies :as cookies]
            [kefirnadar.application.fx :as fx]))

;; TODO: how to make this safer, i.e. protect against identity theft
(defonce user-cookie-name "user-cookie")

(defprotocol AuthMethod
  (get-login-status [this] "Checks whether the current user is logged in with this auth method")
  (handle-login-response [this response] "Finish logging the user in if they were authenticated by the auth provider")
  (log-user-in [this] "Logs the user in using the auth method")
  (log-user-out [this] "Logs the user out using the auth method"))

(defn set-user-cookie-info!
  [user-info]
  (cookies/set! user-cookie-name user-info))

(defn get-user-cookie-info []
  (cookies/get user-cookie-name))

(defn remove-user-cookie-info! []
  (cookies/remove! user-cookie-name))

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
                                     (dispatch [::ensure-user user-info])))))
      #_(dispatch [::set-authentication-data (cond-> {:status status
                                                      :authenticated? authenticated?}
                                               (some? authResponse) (assoc :user-info (js->clj authResponse
                                                                                        :keywordize-keys true)))])))
  (log-user-in [this]
    (j/call js/FB :login #(handle-login-response this %) #js {:scope "public_profile"}))
  (log-user-out [_this]
    (j/call js/FB :logout #(dispatch [:kefirnadar.application.events/dispatch-load-route! {:data {:name :route/home}}]))
    (remove-user-cookie-info!)))

(def auth-methods {:facebook (->FacebookLogin)})

;; TODO: figure out how to check if a user is logged in. This way of doing it would not work for multiple login methods
;; since subsequent login methods would overwrite the result of the previous ones in app db.
(defn get-authentication-statuses! []
  (doseq [auth-method (vals auth-methods)]
    (get-login-status auth-method)))

;; region events
(reg-event-fx ::ensure-user trim-v
  (fn [_ [^::specs/authResponse user-info]]
    {::fx/api {:uri (route-utils/url-for "/api/auth/ensure-user")
               :method :post
               :params user-info
               :on-success [::ensure-user-success]
               :on-error [::ensure-user-fail]}}))

(defn ensure-user-success
  "Stores fetched ads in the app db."
  [db [{:keys [korisnik]}]]
  (let [user-cookie (get-user-cookie-info)]
    (set-user-cookie-info! (assoc user-cookie :korisnik korisnik)))
  db)

(defn ensure-user-fail
  "Failed to fetch the ads, render error page"
  [db _]
  db)

(reg-event-db ::ensure-user-success trim-v ensure-user-success)
(reg-event-db ::ensure-user-fail trim-v ensure-user-fail)

(reg-event-db ::log-user-in trim-v
  (fn [_db [authentication-provider]]
    (log-user-in (authentication-provider auth-methods))))
;; endregion

;; region subs
(reg-sub ::authenticated?
  (fn [_db _]
    (let [{:keys [authenticated?]} (cookies/get user-cookie-name)]
      authenticated?)))

(reg-sub ::user-info
  (fn [_db _]
    (:korisnik (get-user-cookie-info))))

(reg-sub ::user-id :<- [::user-info]
  (fn [user-info _]
    (:id-korisnika user-info)))
;; endregion