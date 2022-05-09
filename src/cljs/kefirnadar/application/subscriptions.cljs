(ns kefirnadar.application.subscriptions
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-sub]]))


;; form subs region
(reg-sub
  ::form
  (fn [db [_ id]]
    (get-in db [:form id])))

(reg-sub
  ::is-valid?
  (fn [db [_ form-ids]]
    (every? #(get-in db [:form %]) form-ids)))
;; end form subs region

;; choice region
(reg-sub
  ::choice
  (fn [db _]
    (get-in db [:user :data :ad-type])))
;; end choice region

