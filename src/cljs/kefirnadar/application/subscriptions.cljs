(ns kefirnadar.application.subscriptions
  (:require [re-frame.core :refer [trim-v reg-event-db reg-event-fx reg-sub]]))

(reg-sub
  ::active-route
  (fn [db _]
    (:active-route db)))

(reg-sub ::form-data (fn [db _] (get-in db [:ads :sharing :form-data])))

(reg-sub
  ::form-field
  (fn [db [_ id]]
    (get-in db [:ads :sharing :form-data id])))

(reg-sub
  ::form-validation
  (fn [db [_ id]]
    (get-in db [:ads :sharing :form-data-validation id])))

(reg-sub
  ::ad-type-choice
  (fn [db _]
    (keyword (get-in db [:active-route :parameters :path :ad-type]))))

(reg-sub
  ::seeking-region
  (fn [db _] (get-in db [:ads :seeking :region-filter])))

(reg-sub ::filtered-ads
  (fn [db _] (get-in db [:ads :seeking :filtered-ads])))

(reg-sub ::grains-kind (fn [db _] (get-in db [:active-route :parameters :path :grains-kind])))
