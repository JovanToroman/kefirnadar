(ns kefirnadar.application.subscriptions
  (:require [re-frame.core :refer [trim-v reg-event-db reg-event-fx reg-sub]]
            [kefirnadar.application.fx :as fx]))


(reg-sub
  ::form
  (fn [db [_ id]]
    (get-in db [:form id] "")))

(reg-sub
  ::is-valid?
  (fn [db [_ form-ids]]
    (every? #(get-in db [:form %]) form-ids)))

(reg-sub
  ::choice
  (fn [db _]
    (get-in db [:user :data :ad-type])))

;; -- da li ovo treba ovde da bude?
(defn fetch-users
  "Fetches all users from the server."
  [_]
  {::fx/api {:uri        "/list"
             :method     :get
             :on-success [::fetch-users-success]}})

(reg-event-fx ::fetch-users fetch-users)

(defn fetch-users-success
  "Stores fetched users in the app db."
  [db [users]]
  (assoc db :all-users users))

(reg-event-db ::fetch-users-success trim-v fetch-users-success)


(reg-sub ::users (fn [db _] (get db :all-users)))


