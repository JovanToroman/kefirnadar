(ns kefirnadar.application.views
  (:require [kefirnadar.application.events :as events]
            [re-frame.core :refer [dispatch]]))

(defn home []
  [:div "Home page"
   [:button {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/test}}])} "Navigate to test page button"]
   [:a {:href "test"} "Navigate to test page anchor"]])

(defn test-view []
  [:div "Test page"])