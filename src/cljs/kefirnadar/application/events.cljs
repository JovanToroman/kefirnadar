(ns kefirnadar.application.events
  (:require [kefirnadar.application.fx :as fx]
            [kefirnadar.configuration.routes :as routes]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-fx trim-v]]
            [cuerdas.core :as str]
            [kefirnadar.configuration.db :as db]
            [kefirnadar.application.validation :as validation]
            [kefirnadar.application.specs :as specs]))


(defn set-dropdown-filtering-value [db [region]]
  (assoc-in db [:ads :seeking :region-filter] region))

(reg-event-db ::set-seeking-region trim-v set-dropdown-filtering-value)

;; -- create ad business logic
(defn validate-and-create-ad
  [{db :db} [grains-kind form-info]]
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
                           :ad/grains-kind (keyword grains-kind)}
                  :on-success [::create-success]}
            assembled-fx-api-body (cond
                                    (and phone-number email) (-> body
                                                               (assoc-in [:params :ad/email] email)
                                                               (assoc-in [:params :ad/phone-number] phone-number))
                                    phone-number (assoc-in body [:params :ad/phone-number] phone-number)
                                    email (assoc-in body [:params :ad/email] email))]
        {::fx/api assembled-fx-api-body})
      {:db (assoc-in db [:ads :sharing :form-data-validation] validation-info)})))

(reg-event-fx ::validate-and-create-ad trim-v validate-and-create-ad)

(defn create-success
  "Dispatched on successful ad creation."
  [_ [_new-ad]]
  {::load-route! {:data {:name :route/thank-you}}})

(reg-event-fx ::create-success trim-v create-success)

;; -- end create business logic

;; -route events-
(reg-fx ::load-route! routes/load-route!)

(defn dispatch-load-route!
  "A coeffect to dispatch the load-route! effect."
  [_ [route _data]]
  {::load-route! route})

(reg-event-fx ::dispatch-load-route! trim-v dispatch-load-route!)
;; -end route events-

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

;; -- end fetch business logic


