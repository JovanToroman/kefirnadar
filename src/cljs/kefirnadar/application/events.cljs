(ns kefirnadar.application.events
  (:require [kefirnadar.application.fx :as fx]
            [kefirnadar.application.localstorage :as localstorage]
            [kefirnadar.configuration.routes :as routes]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-fx trim-v]]))

;; -localstorage events-
(reg-fx ::set-item! localstorage/set-item!)
(reg-fx ::get-item localstorage/get-item)
(reg-fx ::remove-item localstorage/remove-item!)
;; -end localstorage events-


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
               :params     {:user/firstname   #_"Nedeljko"    (get-in db [:form :firstname])
                            :user/lastname    #_"Radovanovic" (get-in db [:form :lastname])
                            :user/region      #_:pancevo      (get-in db [:form :region])
                            :user/post        #_true          (get-in db [:form :post] false)
                            :user/pick-up     #_false         (get-in db [:form :pick-up] false)
                            :user/quantity    #_25            (get-in db [:form :quantity])
                            :user/grains-kind #_:milk-type    (keyword (localstorage/get-item :grains-kind))} ;; js-localstorage sve cuva kao string
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
     ::set-item!   [:ad-type type]
     ::load-route! {:data {:name :route/grains-kind}}}))


(reg-event-fx
  ::grains-kind
  (fn [{db :db} [_ type]]
    {:db           (assoc-in db [:user :data :grains-kind] type)
     ::set-item!   [:grains-kind type]
     ::load-route! {:data {:name :route/choice}}}))


(reg-event-db
  ::add-filter-region
  (fn [db [_ region]]
    (assoc-in db [:user :data :region-filter] region)))


(reg-event-db
  ::update-form
  (fn [db [_ id val]]
    (assoc-in db [:form id] val)))


;; -- fetch business logic
(defn fetch-users
  "Fetches all users from the server."
  [_ [grains-kind region]]
  (js/console.log "grains-kind: " grains-kind " region: " region)
  {::fx/api {:uri        (str "/list/grains-kind/" grains-kind "/region/" region)
             :method     :get
             :on-success [::fetch-users-success]
             :on-error   [::fetch-users-fail]}})

http://localhost:8080/list/grains-kind/:milk-type/region/:Beograd

(reg-event-fx ::fetch-users trim-v fetch-users)

(defn fetch-users-success
  "Stores fetched users in the app db."
  [{db :db} [users]]
  {:db           (assoc db :all-users users)
   ::load-route! {:data {:name :route/list}}})

(defn fetch-users-fail                                      ;; radi- testirano
  "Failed to fetch user's, render error page"
  []
  {::load-route! {:data {:name :route/error}}})


(reg-event-fx ::fetch-users-fail trim-v fetch-users-fail)
(reg-event-fx ::fetch-users-success trim-v fetch-users-success)

;; -- end fetch business logic


