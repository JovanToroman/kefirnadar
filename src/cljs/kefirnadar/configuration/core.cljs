(ns kefirnadar.configuration.core
  (:require [applied-science.js-interop :as j]
            [kefirnadar.configuration.config :as config]
            [kefirnadar.configuration.events :as events]
            [kefirnadar.application.styles :as styles]
            [kefirnadar.application.views :as views]
            [re-frame.core :as re-frame :refer [dispatch-sync]]
            [reagent.dom :as reagent-dom]
            [taoensso.timbre :as timbre]))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (let [app (j/call js/document :getElementById "app")]
    (reagent-dom/render (styles/provider {:main-view views/main-panel}) app)
    (j/call js/Object :assign (j/get app :style) (clj->js (:app styles/styles-map))))
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
  (dispatch-sync [::events/boot])
  (dev-setup)
  (mount-root))

