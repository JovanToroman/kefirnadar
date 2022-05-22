(ns kefirnadar.configuration.subscriptions
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-sub]]))

(reg-sub
  ::active-route
  (fn [db _]
    (:active-route db)))