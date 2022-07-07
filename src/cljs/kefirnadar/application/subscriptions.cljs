(ns kefirnadar.application.subscriptions
  (:require [re-frame.core :refer [trim-v reg-event-db reg-event-fx reg-sub]]))


(reg-sub
  ::form
  (fn [db [_ id]]
    (get-in db [:form id] "")))


(reg-sub
  ::is-id-required?
  (fn [db [_ form-ids]]
    (every? #(get-in db [:form %]) form-ids)))


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


