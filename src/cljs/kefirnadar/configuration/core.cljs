(ns kefirnadar.configuration.core
  (:require [kefirnadar.configuration.config :as config]
            [kefirnadar.configuration.events :as events]
            [kefirnadar.application.events :as app-events]
            [kefirnadar.configuration.views :as views]
            [kefirnadar.application.subscriptions :as subs]
            [re-frame.core :as re-frame :refer [dispatch]]
            [reagent.dom :as reagent-dom]
            [taoensso.timbre :as timbre]))

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
  (dev-setup)
  (mount-root))