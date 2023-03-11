(ns kefirnadar.application.subscriptions
  (:require [re-frame.core :refer [trim-v reg-event-db reg-event-fx reg-sub]]
            [kefirnadar.application.specs :as specs]))

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
  ::seeking-region
  (fn [db _] (get-in db [:ads :seeking :region-filter])))

(reg-sub ::filtered-ads
  (fn [db _] (get-in db [:ads :seeking :filtered-ads :ads])))

(reg-sub ::ads-count
  (fn [db _] (get-in db [:ads :seeking :filtered-ads :ads-count])))

(reg-sub ::grains-kind
  (fn [db [_ ^::specs/user-action user-action]]
    (get-in db [:ads user-action :grains-kind])))

(reg-sub ::ads-pagination-info
  (fn [db [_ action]]
    (get-in db [:ads action :pagination-info])))
