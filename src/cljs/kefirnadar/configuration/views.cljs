(ns kefirnadar.configuration.views
  (:require
    [kefirnadar.application.styles :as styles]
    [kefirnadar.application.views :as application-views]
    [kefirnadar.configuration.subscriptions :as subscriptions]
    [re-frame.core :refer [subscribe]]))

(defn- panels [panel-name]
  (case (:name (:data panel-name))
    :route/home [application-views/home]
    ;; -----
    :route/ad-type [application-views/grains-kind]
    ;; -----
    :route/ad-type-choice [application-views/ad-type-choice]
    ;; -----
    :route/thank-you [application-views/thank-you]
    ;; -----
    :route/error [application-views/error]
    [:div]))

(defn main-panel []
  (let [active-panel @(subscribe [::subscriptions/active-route])
        [css] (styles/use-styletron)]
    [:div
     [:header
      [:h1.d-flex.justify-content-center.align-items-center {:className (css #js {:color "blue"})} "Kefirnadar"]]
     [panels active-panel]
     [:footer
      [:h3 "Footer"]]]))
