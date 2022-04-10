(ns kefirnadar.configuration.core
  (:require [re-frame.core :as re-frame :refer [dispatch]]
            [reagent.dom :as reagent-dom]
            [kefirnadar.configuration.config :as config]
            [taoensso.timbre :as timbre]
            [kefirnadar.configuration.views :as views]
            [kefirnadar.configuration.events :as events]))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent-dom/render [views/main-panel]
    (.getElementById js/document "app"))
  (timbre/info "App version:" config/version))

(defn ^:after-load re-render
  "Re-render the app when figwheel reloads"
  []
  (mount-root))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn ^:export start []
  (dispatch [::events/boot])
  (dispatch [::events/navigate-to-initial-route])
  (dev-setup)
  (mount-root))