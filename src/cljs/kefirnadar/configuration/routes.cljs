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
   ["" {:name        :route/home
        :controllers [{:identity identity}]
        :doc         "Home page"}]
   ;; -----
   ["grains-kind"
    {:name        :route/grains-kind
     :doc         "Grains kind page"
     :controllers [{:identity identity}]}]
   ["choice"
    {:name        :route/choice
     :doc         "Form page"
     :controllers [{:identity identity}]}]
   ["form"
    {:name        :route/form
     :doc         "form page"
     :controllers [{:identity identity}]}]
   ["thank-you"
    {:name        :route/thank-you
     :doc         "Thank you page"
     :controllers [{:identity identity}]}]
   ["list"
    {:name        :route/list
     :doc         "list page"
     :controllers [{:identity identity}]}]
   ["user-detail"
    {:name        :route/user-detail
     :doc         "user detail page"
     :controllers [{:identity identity}]}]
   ["error"
    {:name        :route/error
     :doc         "error page"
     :controllers [{:identity identity}]}]])

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

(defn load-route! [{:keys [data path-params query-params replace params] :as _route}]
  (redirect! {:name         (-> data :name)
              :params         params
              :path-params  path-params
              :query-params query-params
              :replace      replace}))
