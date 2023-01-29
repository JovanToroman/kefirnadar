(ns kefirnadar.configuration.routes
  (:require [kefirnadar.application.subscriptions :as subs]
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
                       :start #(dispatch [:kefirnadar.application.events/clean-db])}]
        :doc "Home page"}]
   ["sharing/"
    ["" {:name :route/sharing
         :doc "Page where users choose which grain kind to share"}]
    ["grains-kind/{grains-kind}"
     {:name :route/share-grains-form
      :path-params {:grains-kind keyword?}
      :doc "Form to share grains"
      :controllers [{:parameters {:path [:grains-kind]}
                     :start (fn [{{grains-kind :grains-kind} :path}]
                              (dispatch [:kefirnadar.application.events/store-grains-kind (keyword grains-kind)
                                         :sharing]))
                     :stop #(js/console.log "STOP: " "ad-type/:ad-type/grains-kind/:grains-kind")}]}]]
   ["seeking/"
    ["" {:name :route/seeking
         :doc "Page where users browse existing grains ads"}]
    ["grains-kind/{grains-kind}"
     {:name :route/search-for-grains
      :path-params {:grains-kind keyword?}
      :doc ""
      :controllers [{:parameters {:path [:grains-kind]}
                     :start (fn [{{grains-kind :grains-kind} :path}]
                              (dispatch [:kefirnadar.application.events/store-grains-kind (keyword grains-kind)
                                         :seeking]))
                     :stop #(js/console.log "STOP: " "ad-type/:ad-type/grains-kind/:grains-kind")}]}]]
   ["thank-you"
    {:name :route/thank-you
     :doc "Thank you page"
     :controllers [{:identity identity}]}]
   ["error"
    {:name :route/error
     :doc "error page"
     :controllers [{:identity identity}]}]])

(defonce router (rf/router routes))
;endregion

; setup router
(defn init! []
  (rfe/start!
    router
    (fn [route]
      (let [old-route @(subscribe [::subs/active-route])]
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

(defn load-route! [{:keys [data path-params query-params replace params] :as _route}]
  (redirect! {:name         (-> data :name)
              :params       params
              :path-params  path-params
              :query-params query-params
              :replace      replace}))
