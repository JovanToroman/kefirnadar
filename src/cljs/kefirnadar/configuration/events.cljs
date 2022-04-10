(ns kefirnadar.configuration.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx]]
            [kefirnadar.configuration.db :as db]))

(reg-event-db ::boot
  (fn [_ _]
    db/default-db))