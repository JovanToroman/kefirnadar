(ns kefirnadar.application.events
  (:require [kefirnadar.application.fx :as fx]
            [kefirnadar.configuration.routes :as routes]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-fx trim-v]]))


;; helper functions
(defn dropdown-filtering-value [db [_ region]]
  (assoc-in db [:user :data :region-filter] region))

(reg-event-db
  ::dropdown-filtering-value
  dropdown-filtering-value)
;;end helper functions

;; -- create user business logic
(defn create
  "Persists a user to the server."
  [{db :db} [_params]]
  (let [type (get-in db [:user :data :grains-kind])
        form-data (assoc (:form db) :grains-kind type)
        body {:uri        "/create"
              :method     :post
              :params     {:ad/firstname   (get-in db [:form :firstname])
                           :ad/lastname    (get-in db [:form :lastname])
                           :ad/region      (get-in db [:form :region])
                           :ad/post        (get-in db [:form :post] false)
                           :ad/pick-up     (get-in db [:form :pick-up] false)
                           :ad/quantity    (get-in db [:form :quantity])
                           :ad/grains-kind (keyword (get-in db [:active-route :parameters :path :grains-kind]))}
              :on-success [::create-success]}
        assembled-fx-api-body (cond
                                (and (get-in db [:form :phone-number])
                                     (get-in db [:form :email])) (-> body
                                                                     (assoc-in [:params :ad/email] (get-in db [:form :email]))
                                                                     (assoc-in [:params :ad/phone-number] (get-in db [:form :phone-number])))
                                (get-in db [:form :phone-number]) (assoc-in body [:params :ad/phone-number] (get-in db [:form :phone-number]))
                                (get-in db [:form :email]) (assoc-in body [:params :ad/email] (get-in db [:form :email])))]
    {:db      (-> db
                  (assoc-in [:user :form] form-data)
                  (dissoc :form))
     ::fx/api assembled-fx-api-body}))

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
  ::grains-kind trim-v
  (fn [{db :db} [type]]
    (js/console.log "Type: " type)
    {:db           (assoc-in db [:user :data :grains-kind] type)
     ::load-route! {:data        {:name :route/ad-type-choice}
                    :path-params {:grains-kind type
                                  :ad-type     (get-in db [:user :data :ad-type])}}}))


(defn clean-db
  "Kada se klikne na Pocetna stranica dugme brise se iz local db-a sve neophodno da bi se resili konflikta"
  [{db :db}]
  {:db (dissoc db :all-users :user)})

(reg-event-fx
  ::clean-db
  clean-db)

(reg-event-db
  ::update-form
  (fn [db [_ id val]]
    (assoc-in db [:form id] val)))

(reg-event-db
  ::store-validation-results
  (fn [db [_ id val]]
    (assoc-in db [:form-validation id] val)))

(defn fetch-users
  "Fetches all users from the server."
  [{db :db} [region]]
  (let [grains-kind (get-in db [:active-route :parameters :path :grains-kind])]
    {:db (-> db
           (assoc-in [:user :data :region-filter] region))
     ::fx/api {:uri (str "/list/grains-kind/" grains-kind "/region/" region)
               :method :get
               :on-success [::fetch-users-success]
               :on-error [::fetch-users-fail]}}))


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


