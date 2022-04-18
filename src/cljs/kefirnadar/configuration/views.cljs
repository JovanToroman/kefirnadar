(ns kefirnadar.configuration.views
  (:require [kefirnadar.application.views :as application-views]
            [kefirnadar.configuration.subscriptions :as subscriptions]
            [re-frame.core :refer [subscribe]]))

(defn- panels [panel-name]
  (case (:name (:data panel-name))
    :route/home [application-views/home]
    :route/test [application-views/test-view]
    [:div]))

(defn main-panel []
  (let [active-panel @(subscribe [::subscriptions/active-route])]
    [:div
     [panels active-panel]]))
