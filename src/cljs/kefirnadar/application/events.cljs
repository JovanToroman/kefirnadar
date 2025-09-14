(ns kefirnadar.application.events
  (:require [kefirnadar.application.fx :as fx]
            [re-frame.core :refer [reg-event-db reg-event-fx reg-fx trim-v]]
            [kefirnadar.application.db :as db]
            [kefirnadar.application.validation :as validation]
            [kefirnadar.application.utils.route :as route-utils]
            [kefirnadar.application.specs :as specs]
            [kefirnadar.common.utils :refer-macros [-m]]
            [reitit.frontend.easy :as rfe]
            [taoensso.timbre :as log]))

(defn validate-and-create-ad
  [{db :db} [form-info id-korisnika]]
  (let [validation-info (validation/either-or-update-validation (validation/validate-form-info form-info))]
    (if (validation/sharing-form-valid? validation-info)
      (let [{:keys [oblast kontakt nacin-deljenja vrsta-kulture]} form-info
            body {:uri "/api/oglas/dodaj"
                  :method :post
                  :params {:ad/oblast oblast
                           :ad/imejl (:imejl kontakt)
                           :ad/broj-telefona (:broj-telefona kontakt)
                           :ad/slanje? (:slanje? nacin-deljenja)
                           :ad/preuzimanje? (:preuzimanje? nacin-deljenja)
                           :ad/deli-mlecni? (:deli-mlecni? vrsta-kulture)
                           :ad/deli-vodeni? (:deli-vodeni? vrsta-kulture)
                           :ad/deli-kombucu? (:deli-kombucu? vrsta-kulture)
                           :korisnik/id id-korisnika}
                  :on-success [::validate-and-create-ad-success]}]
        {::fx/api body})
      {:db (assoc-in db [:ads :sharing :form-data-validation] validation-info)})))

(reg-event-fx ::validate-and-create-ad trim-v validate-and-create-ad)

(reg-event-fx ::validate-and-create-ad-success trim-v
  (fn [_ _]
    {::load-route! {:data {:name :route/thank-you}}}))

(reg-event-db ::oznaci-polje-kao-nepotvrdjeno trim-v
  (fn [db [kljuc-polja]]
    (assoc-in db [:ads :sharing :form-data-validation kljuc-polja] false)))

(reg-event-db ::oznaci-polje-kao-potvrdjeno trim-v
  (fn [db [kljuc-polja]]
    (assoc-in db [:ads :sharing :form-data-validation kljuc-polja] true)))

;; region routing
(defn redirect!
  "If `replace` is truthy, previous page will be replaced in history, otherwise added."
  [{:keys [name path-params query-params replace]}]
  ;; query params returns an empty map {} and replace-state and push-state are multiarity
  ;; this if-let prevents a lonely ? from being appended to the url if there are no query params
  (if-let [query-params (not-empty query-params)]
    (if replace
      (rfe/replace-state name path-params query-params)
      (rfe/push-state name path-params query-params))
    (if replace
      (rfe/replace-state name path-params)
      (rfe/push-state name path-params))))

(defn load-route! [{:keys [data path-params query-params replace params] :as _route}]
  (redirect! {:name         (-> data :name)
              :params       params
              :path-params  path-params
              :query-params query-params
              :replace      replace}))

(reg-fx ::load-route! load-route!)

(defn dispatch-load-route!
  "A coeffect to dispatch the load-route! effect."
  [_ [route _data]]
  {::load-route! route})

(reg-event-fx ::dispatch-load-route! trim-v dispatch-load-route!)
;; endregion

(defn clean-db
  [db _]
  (merge db db/default-db))

(reg-event-db ::clean-db clean-db)

(reg-event-db
  ::update-sharing-form
  (fn [db [_ id val]]
    (if (map? val)
      (update-in db [:ads :sharing :form-data id] merge val)
      (assoc-in db [:ads :sharing :form-data id] val))))

(reg-event-db
  ::promeni-polje-za-unos-oglasa trim-v
  (fn [db [odredisno-polje]]
    (assoc-in db [:ads :sharing :trenutno-polje-za-unos-oglasa] odredisno-polje)))

(reg-event-db
  ::store-sharing-form-validation-results
  (fn [db [_ id val]]
    (assoc-in db [:ads :sharing :form-data-validation id] val)))

(reg-event-db
  ::azuriraj-formu-registracije
  (fn [db [_ id val]]
    (assoc-in db [:ads :registracija :form-data id] val)))

(reg-event-db
  ::azuriraj-formu-prijave
  (fn [db [_ id val]]
    (assoc-in db [:ads :prijava :form-data id] val)))

(reg-event-db
  ::azuriraj-formu-za-slanje-imejla-za-resetovanje-lozinke
  (fn [db [_ id val]]
    (assoc-in db [:ads :slanje-imejla-za-resetovanje-lozinke :form-data id] val)))

(reg-event-db
  ::azuriraj-formu-za-resetovanje-lozinke
  (fn [db [_ id val]]
    (assoc-in db [:ads :resetovanje-lozinke :form-data id] val)))

(reg-event-db ::azuriraj-kontakt-formu
  (fn [db [_ id val]]
    (assoc-in db [:ads :kontakt :podaci-forme id] val)))

(reg-event-fx ::posalji-kontakt-poruku trim-v
  (fn [{:keys [db]} [kontakt-podaci]]
    (let [rezultat-provere (validation/validate-form-info kontakt-podaci)]
      (if (validation/forma-validna? rezultat-provere)
      {:db (update-in db [:ads :kontakt :provera-podataka-forme] dissoc :poruka)
       ::fx/api {:uri "/api/posalji-kontakt-poruku"
                 :method :post
                 :params kontakt-podaci
                 :on-success [::posalji-kontakt-poruku-uspeh]
                 :on-error [::posalji-kontakt-poruku-neuspeh]}}
      {:db (assoc-in db [:ads :kontakt :provera-podataka-forme] rezultat-provere)}))))

(reg-event-fx ::posalji-kontakt-poruku-uspeh
  (fn [_ _]
    {:kefirnadar.application.events/load-route! {:data {:name :route/nakon-slanja-kontakt-poruke}}}))

(reg-event-db ::posalji-kontakt-poruku-neuspeh
  (fn [db _]
    (assoc-in db [:ads :kontakt :provera-podataka-forme :poruka] false)))


(reg-event-fx ::fetch-ads trim-v
  (fn [_ [^::specs/filters filters ^::specs/pagination-info pagination-info]]
    {::fx/api {:uri (route-utils/url-for "/api/oglasi" :query (merge filters pagination-info) :path {})
               :method :get
               :on-success [::fetch-ads-success]
               :on-error [::fetch-ads-fail]}}))

(reg-event-db ::fetch-ads-success trim-v
  (fn [db [{:keys [ads ads-count]}]]
    (assoc-in db [:ads :seeking :filtered-ads] (-m ads ads-count))))

(reg-event-db ::fetch-ads-fail trim-v
  (fn [_ odgovor]
    (log/error "Greška u uzimanju oglasa: " odgovor)))

(reg-event-fx ::moji-oglasi trim-v
  (fn [{db :db} _]
    (if-some [id-korisnika (get-in db [:auth :korisnik :id-korisnika])]
      {::fx/api {:uri (route-utils/url-for "/api/oglasi" :query {:id-korisnika id-korisnika})
                 :method :get
                 :on-success [::moji-oglasi-uspeh]
                 :on-error [::moji-oglasi-neuspeh]}}
      ;; TODO: naći rešenje: kada osvežim stranicu ne mogu da dobijem id korisnika jer nije još učitan
      {::load-route! {:data {:name :route/home}}})))

(reg-event-db ::moji-oglasi-uspeh trim-v
  (fn [db [{:keys [ads ads-count]}]]
    (assoc-in db [:ads :moji-oglasi] (-m ads ads-count))))

(reg-event-db ::moji-oglasi-neuspeh trim-v
  (fn [_ odgovor]
    (log/error "Greška u uzimanju oglasa: " odgovor)))

(reg-event-fx ::izbrisi-oglas trim-v
  (fn [_ [id-oglasa]]
    {::fx/api {:uri (route-utils/url-for "/api/oglas/izbrisi/%s" :path [id-oglasa])
               :method :delete
               :on-success [::izbrisi-oglas-uspeh id-oglasa]
               :on-error [::izbrisi-oglas-neuspeh]}}))

(reg-event-db ::izbrisi-oglas-uspeh trim-v
  (fn [db [id-oglasa]]
    (update-in db [:ads :moji-oglasi :ads] #(remove (comp #{id-oglasa} :ad/ad_id) %))))

(reg-event-db ::izbrisi-oglas-neuspeh trim-v
  (fn [_ odgovor]
    (log/error "Greška u brisanju oglasa: " odgovor)))

(reg-event-db ::store-ads-pagination-info trim-v
  (fn [db [action pagination-info]]
    (assoc-in db [:ads action :pagination-info] pagination-info)))

(reg-event-db ::store-show-filters
  (fn [db [_ current]]
    (assoc-in db [:ads :seeking :show-filters?] (not current))))

(reg-event-fx ::apply-filters trim-v
  (fn [{:keys [db]} [filters]]
    (let [{:keys [path-params]} (:active-route db)]
      {:db (assoc-in db [:ads :seeking :show-filters?] false)
       ::load-route! {:data {:name :route/trazim}
                      :query-params filters
                      :path-params path-params}})))

(defn filters-update-functions [filter-key]
  (case filter-key
    :regions (fn [_current added]
               ;; TODO: change to support multiple selected regions
               #{added})
    nil merge
    (fn [_ value] value)))

;; TODO: refactor this to multiples effects, one for each case
(reg-event-db ::update-filters trim-v
  (fn [db [value filter-key]]
    (update-in db (cond-> [:ads :seeking :filters]
                    (some? filter-key) (conj filter-key)) (filters-update-functions filter-key) value)))

(reg-event-db ::reset-filters
  (fn [db _]
    (assoc-in db [:ads :seeking :filters] (get-in db/default-db [:ads :seeking :filters]))))

(reg-event-db ::set-ads-meta trim-v
  (fn [db [ad-id meta-key value]]
    (assoc-in db [:ads :seeking :ads-meta ad-id meta-key] value)))
