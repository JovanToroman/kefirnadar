(ns kefirnadar.configuration.views
  (:require [kefirnadar.application.views :as application-views]
            [kefirnadar.configuration.subscriptions :as subscriptions]
            [re-frame.core :refer [subscribe]]))

(defn- panels [panel-name]
  (case (:name (:data panel-name))
    :route/home [application-views/home]
    ;; -----
    :route/grains-kind [application-views/grains-kind]
    ;; -----
    :route/choice [application-views/choice]
    [:div]))

(defn main-panel []
  (let [active-panel @(subscribe [::subscriptions/active-route])]
    [:div
     [:header
      [:h1 "Header"]]
     [panels active-panel]
     [:footer
      [:h3 "Footer"]]]))
