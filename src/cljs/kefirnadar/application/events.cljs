(ns kefirnadar.application.events
  (:require [kefirnadar.application.fx :as fx]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-fx trim-v]]
            [kefirnadar.application.db :as db]
            [kefirnadar.application.validation :as validation]
            [kefirnadar.application.utils.route :as route-utils]
            [kefirnadar.application.specs :as specs]
            [kefirnadar.application.auth :as auth]
            [kefirnadar.common.utils :refer-macros [-m]]
            [reitit.frontend.easy :as rfe]))

(defn validate-and-create-ad
  [{db :db} [form-info id-korisnika]]
  (let [validation-info (validation/either-or-update-validation (validation/validate-form-info form-info))]
    (if (validation/sharing-form-valid? validation-info :region :quantity)
      (let [{:keys [region post? pick-up? quantity phone-number email sharing-milk-type?
                    sharing-water-type? sharing-kombucha?]} form-info
            body {:uri "/api/create"
                  :method :post
                  :params {:ad/region region
                           :ad/email email
                           :ad/phone-number phone-number
                           :ad/post? post?
                           :ad/pick-up? pick-up?
                           :ad/quantity quantity
                           :ad/sharing-milk-type? sharing-milk-type?
                           :ad/sharing-water-type? sharing-water-type?
                           :ad/sharing-kombucha? sharing-kombucha?
                           :korisnik/id id-korisnika}
                  :on-success [::validate-and-create-ad-success]}]
        {::fx/api body})
      {:db (assoc-in db [:ads :sharing :form-data-validation] validation-info)})))

(reg-event-fx ::validate-and-create-ad trim-v validate-and-create-ad)

(reg-event-fx ::validate-and-create-ad-success trim-v
  (fn [_ _]
    {::load-route! {:data {:name :route/thank-you}}}))

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
    (assoc-in db [:ads :sharing :form-data id] val)))

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

(defn fetch-ads
  [_ [^::specs/filters filters ^::specs/pagination-info pagination-info]]
  {::fx/api {:uri (route-utils/url-for "/api/list" :query (merge filters pagination-info) :path {})
             :method :get
             :on-success [::fetch-ads-success]
             :on-error [::fetch-ads-fail]}})

(reg-event-fx ::fetch-ads trim-v fetch-ads)

(defn fetch-ads-success
  "Stores fetched ads in the app db."
  [db [{:keys [ads ads-count]}]]
  (assoc-in db [:ads :seeking :filtered-ads] (-m ads ads-count)))

(defn fetch-ads-fail
  "Failed to fetch the ads, render error page"
  [_ _]
  #_{::load-route! {:data {:name :route/error}}})

(reg-event-db ::fetch-ads-success trim-v fetch-ads-success)
(reg-event-db ::fetch-ads-fail trim-v fetch-ads-fail)

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
       ::load-route! {:data {:name :route/seeking}
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
