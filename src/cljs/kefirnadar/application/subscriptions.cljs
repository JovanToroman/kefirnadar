(ns kefirnadar.application.subscriptions
  (:require [re-frame.core :refer [trim-v reg-event-db reg-event-fx reg-sub]]))


(reg-sub
  ::form
  (fn [db [_ id]]
    (get-in db [:form id] "")))

(reg-sub
  ::form-validation
  (fn [db [_ id]]
    (get-in db [:form-validation id] true)))


;; helper function for ::is-valid?

(defn contains-any? [map & keys]
  (some #(contains? map %) keys))


(reg-sub
  ::is-valid?
  (fn [db [_ form-ids]]
    (and (every? #(get-in db [:form %]) form-ids)
         (every? #(true? (val %)) (:form-validation db))
         (contains-any? (:form db) :post :pick-up)
         (contains-any? (:form db) :phone-number :email))))


(reg-sub
  ::ad-type-choice
  (fn [db _]
    (keyword (get-in db [:active-route :parameters :path :ad-type]))))


(reg-sub
  ::users
  (fn [db _] (get db :all-users)))


(reg-sub
  ::region
  (fn [db _] (get-in db [:user :data :region-filter] "")))


