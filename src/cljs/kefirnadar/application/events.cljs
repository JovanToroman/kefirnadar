(ns kefirnadar.application.events
  (:require [kefirnadar.application.fx :as fx]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-fx trim-v]]
            [cuerdas.core :as str]
            [kefirnadar.configuration.db :as db]
            [kefirnadar.application.validation :as validation]
            [kefirnadar.application.specs :as specs]
            [reitit.frontend.easy :as rfe]))


(defn set-dropdown-filtering-value [db [region]]
  (assoc-in db [:ads :seeking :region-filter] region))

(reg-event-db ::set-seeking-region trim-v set-dropdown-filtering-value)

;; -- create ad business logic
(defn validate-and-create-ad
  [{db :db} [grains-kind form-info user-id]]
  (let [validation-info (validation/validate-form-info form-info)]
    (if (validation/form-valid? validation-info :firstname :lastname :region :quantity)
      (let [{:keys [firstname lastname region post? pick-up? quantity phone-number email]} form-info
            body {:uri "/create"
                  :method :post
                  :params {:ad/firstname firstname
                           :ad/lastname lastname
                           :ad/region region
                           :ad/post? (or post? false)
                           :ad/pick-up? (or pick-up? false)
                           :ad/quantity quantity
                           :ad/grains-kind grains-kind
                           :user-id user-id}
                  :on-success [::validate-and-create-ad-success]}
            assembled-fx-api-body (cond
                                    (and phone-number email) (-> body
                                                               (assoc-in [:params :ad/email] email)
                                                               (assoc-in [:params :ad/phone-number] phone-number))
                                    phone-number (assoc-in body [:params :ad/phone-number] phone-number)
                                    email (assoc-in body [:params :ad/email] email))]
        {::fx/api assembled-fx-api-body})
      {:db (assoc-in db [:ads :sharing :form-data-validation] validation-info)})))

(reg-event-fx ::validate-and-create-ad trim-v validate-and-create-ad)

(defn validate-and-create-ad-success
  "Dispatched on successful ad creation."
  [_ [_new-ad]]
  {::load-route! {:data {:name :route/thank-you}}})

(reg-event-fx ::validate-and-create-ad-success trim-v validate-and-create-ad-success)

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

(reg-event-db
  ::store-grains-kind trim-v
  (fn [db [grains-kind ^::specs/user-action user-action]]
    (assoc-in db [:ads user-action :grains-kind] grains-kind)))


(defn clean-db
  [_ _]
  db/default-db)

(reg-event-db ::clean-db clean-db)

(reg-event-db
  ::update-sharing-form
  (fn [db [_ id val]]
    (assoc-in db [:ads :sharing :form-data id] val)))

(reg-event-db
  ::store-sharing-form-validation-results
  (fn [db [_ id val]]
    (assoc-in db [:ads :sharing :form-data-validation id] val)))

(defn fetch-ads
  "Fetches all ads matching the specified region."
  [_ [region grains-kind]]
  {::fx/api {:uri (str/format "/list/grains-kind/%s/region/%s" grains-kind region)
             :method :get
             :on-success [::fetch-ads-success]
             :on-error [::fetch-ads-fail]}})


(reg-event-fx ::fetch-ads trim-v fetch-ads)

(defn fetch-ads-success
  "Stores fetched ads in the app db."
  [db [ads]]
  (assoc-in db [:ads :seeking :filtered-ads] ads))

(defn fetch-ads-fail
  "Failed to fetch the ads, render error page"
  [_ _]
  {::load-route! {:data {:name :route/error}}})
(reg-event-db ::fetch-ads-success trim-v fetch-ads-success)
