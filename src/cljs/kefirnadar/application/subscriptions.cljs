(ns kefirnadar.application.subscriptions
  (:require [re-frame.core :refer [trim-v reg-event-db reg-event-fx reg-sub]]))

(reg-sub ::active-route
  (fn [db _]
    (:active-route db)))

(reg-sub ::form-data (fn [db _] (get-in db [:ads :sharing :form-data])))

(reg-sub ::form-field
  (fn [db [_ id]]
    (get-in db [:ads :sharing :form-data id])))

(reg-sub ::form-validation
  (fn [db [_ id]]
    (get-in db [:ads :sharing :form-data-validation id])))

(reg-sub ::filters
  (fn [db [_ filter-key]] (get-in db (cond-> [:ads :seeking :filters]
                                       (some? filter-key) (conj filter-key)))))

(reg-sub ::ads-meta
  (fn [db [_ ad-id meta-key]] (get-in db (cond-> [:ads :seeking :ads-meta ad-id]
                                           (keyword? meta-key) (conj meta-key)))))

(reg-sub ::filtered-ads
  (fn [db _] (get-in db [:ads :seeking :filtered-ads :ads])))

(reg-sub ::ads-count
  (fn [db _] (get-in db [:ads :seeking :filtered-ads :ads-count])))

(reg-sub ::ads-pagination-info
  (fn [db _]
    (get-in db [:ads :seeking :pagination-info])))

(reg-sub ::show-filters?
  (fn [db _]
    (get-in db [:ads :seeking :show-filters?])))
