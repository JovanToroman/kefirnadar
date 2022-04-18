(ns kefirnadar.application.events
  (:require [kefirnadar.configuration.routes :as routes]
            [re-frame.core :refer [reg-event-fx reg-fx trim-v]]))

(reg-fx ::load-route! routes/load-route!)

(defn dispatch-load-route!
  "A coeffect to dispatch the load-route! effect."
  [_ [route]]
  {::load-route! route})

(reg-event-fx ::dispatch-load-route! [trim-v] dispatch-load-route!)