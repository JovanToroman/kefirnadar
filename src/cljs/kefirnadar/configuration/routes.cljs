(ns kefirnadar.configuration.routes
  (:require [kefirnadar.configuration.subscriptions :as subscriptions]
            [re-frame.core :refer [dispatch subscribe]]
            [reitit.coercion.schema]
            [reitit.coercion.spec]
            [reitit.frontend :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]))

; region routes
(def routes
  ["/" {:coercion reitit.coercion.spec/coercion}
   ["" {:name :route/home
        :controllers [{:identity identity
                       :start (fn [_match] (js/console.log "Usli smo u home rutu"))
                       :stop (fn [_match] (js/console.log "Izasli smo iz home rutu"))}]
        :doc "Home page"}]
   ["test" {:name :route/test
            :doc "test page"
            :controllers [{:identity identity
                           :start (fn [_match] (js/console.log "Usli smo u test rutu"))
                           :stop (fn [_match] (js/console.log "Izasli smo iz test rutu"))}]}]])

(defonce router (rf/router routes))
;endregion

; setup router
(defn init! []
  (rfe/start!
    router
    (fn [route]
      (let [old-route @(subscribe [::subscriptions/active-route])]
        (dispatch [:kefirnadar.configuration.events/set-active-route
                   (assoc route :controllers (rfc/apply-controllers (:controllers old-route) route))])))
    {:use-fragment false}))

(defn redirect!
  "If `replace` is truthy, previous page will be replaced in history, otherwise added."
  [{:keys [name path-params query-params replace]}]
  ;; query params returns an empty map {} and replace-state and push-state are multiarity
  ;; this if-let prevents a lonely ? from being appended to the url if there are no query params
  (if-let [query-params (not-empty query-params)]
    (if replace
      (rfe/replace-state name path-params query-params)
      (rfe/push-state name path-params query-params))
    (if replace
      (rfe/replace-state name path-params)
      (rfe/push-state name path-params))))

(defn load-route! [{:keys [data path-params query-params replace] :as _route}]
  (redirect! {:name (-> data :name)
              :path-params path-params
              :query-params query-params
              :replace replace}))