(ns kefirnadar.configuration.events
  (:require [kefirnadar.configuration.db :as db]
            [kefirnadar.configuration.routes :as routes]
            [re-frame.core :refer [reg-event-db reg-event-fx reg-sub]]))

; not needed at the moment
(reg-event-db ::boot
  (fn [_ _]
    (routes/init!)
    db/default-db))

(reg-event-db
  ::set-active-route
  (fn [db [_ active-panel _opts]]
    (let [old-route (get db :active-route)]
      (assoc db
        :old-route old-route
        :active-route active-panel))))
