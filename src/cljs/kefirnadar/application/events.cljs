(ns kefirnadar.application.events
  (:require [kefirnadar.application.fx :as fx]
            [kefirnadar.configuration.routes :as routes]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-fx trim-v]]))


;; helper functions
(defn add-filter-region! [db region]
  (assoc-in db [:user :data :region-filter] region))

(reg-fx
  ::add-filter-region
  add-filter-region!)
;;end helper functions

;; -- create user business logic
(defn create
  "Persists a user to the server."
  [{db :db} [_params]]
  (let [type (get-in db [:user :data :grains-kind])
        form-data (assoc (:form db) :grains-kind type)]
    {:db      (-> db
                  (assoc-in [:user :form] form-data)
                  (dissoc :form))
     ::fx/api {:uri        "/create"
               :method     :post
               :params     {:user/firstname    (get-in db [:form :firstname])
                            :user/lastname     (get-in db [:form :lastname])
                            :user/region       (get-in db [:form :region])
                            :user/post         (get-in db [:form :post] false)
                            :user/pick-up      (get-in db [:form :pick-up] false)
                            :user/quantity     (get-in db [:form :quantity])
                            :user/phone-number (get-in db [:form :phone-number] "NOT PROVIDED")
                            :user/email       (get-in db [:form :email] "NOT PROVIDED")
                            :user/grains-kind  (keyword (get-in db [:active-route :parameters :path :grains-kind]))}
               :on-success [::create-success]}}))

(reg-event-fx ::create create)

(defn create-success
  "Dispatched on successful user creation."
  [{db :db} [new-user]]
  {:db           (assoc db :users new-user)
   ::load-route! {:data {:name :route/thank-you}}})

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


(reg-event-fx
  ::ad-type
  (fn [{db :db} [_ {type :type}]]
    {:db           (assoc-in db [:user :data :ad-type] type)
     ::load-route! {:data        {:name :route/ad-type}
                    :path-params {:ad-type type}}}))


(reg-event-fx
  ::grains-kind
  (fn [{db :db} [_ type]]
    {:db           (assoc-in db [:user :data :grains-kind] type)
     ::load-route! {:data        {:name :route/ad-type-choice}
                    :path-params {:grains-kind type
                                  :ad-type     (get-in db [:user :data :ad-type])}}}))


;; just a quick-fix
(defn clean-db-when-homepage
  "Kada se klikne na Pocetna stranica dugme brise se iz local db-a sve neophodno da bi se resili konflikta"
  [{db :db}]
  {:db           (dissoc db :all-users :user)})

(reg-event-fx
  ::clean-db-when-homepage
  clean-db-when-homepage)


(reg-event-db
  ::update-form
  (fn [db [_ id val]]
    (assoc-in db [:form id] val)))

(reg-event-db
  ::validate-form
  (fn [db [_ id val]]
    (assoc-in db [:form-validation id] val)))

(defn fetch-users
  "Fetches all users from the server."
  [{db :db} [region]]
  (let [grains-kind (keyword (get-in db [:active-route :parameters :path :grains-kind]))]
    {:db      (-> db
                  (assoc-in [:user :data :region-filter] region))
     ::fx/api {:uri        (str "/list/grains-kind/" grains-kind "/region/" region)
               :method     :get
               :on-success [::fetch-users-success]
               :on-error   [::fetch-users-fail]}}))


(reg-event-fx ::fetch-users trim-v fetch-users)

(defn fetch-users-success
  "Stores fetched users in the app db."
  [{db :db} [users]]
  {:db (assoc db :all-users users)})

(defn fetch-users-fail
  "Failed to fetch user's, render error page"
  []
  {::load-route! {:data {:name :route/error}}})


(reg-event-fx ::fetch-users-fail trim-v fetch-users-fail)
(reg-event-fx ::fetch-users-success trim-v fetch-users-success)

;; -- end fetch business logic


