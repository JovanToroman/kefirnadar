(ns kefirnadar.application.routes
  (:require [kefirnadar.application.subscriptions :as subs]
            [kefirnadar.application.auth :as auth]
            [kefirnadar.application.events :as events]
            [kefirnadar.common.utils :refer-macros [-m]]
            [re-frame.core :refer [dispatch subscribe]]
            [reitit.coercion.schema]
            [reitit.coercion.spec]
            [spec-tools.data-spec :as ds]
            [reitit.frontend :as rf]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]))

; region routes
(def routes
  ["/" {:coercion reitit.coercion.spec/coercion}
   ["" {:name :route/home
        :controllers [{:identity identity
                       :start #(dispatch [::events/clean-db])}
                      #_{:identity identity
                       :start (fn [] (auth/get-authentication-statuses!))}]
        :doc "Home page"}]
   ["sharing/"
    ["" {:name :route/sharing
         :doc "Page where users choose which grain kind to share"}]
    ["grains-kind/{grains-kind}"
     {:name :route/share-grains-form
      :parameters {:path {:grains-kind keyword?}}
      :doc "Form to share grains"
      :controllers [{:parameters {:path [:grains-kind]}
                     :start (fn [{{grains-kind :grains-kind} :path}]
                              (dispatch [::events/store-grains-kind grains-kind :sharing]))
                     :stop #(js/console.log "STOP: " "ad-type/:ad-type/grains-kind/:grains-kind")}]}]]
   ["seeking/"
    ["" {:name :route/seeking
         :doc "Page where users browse existing grains ads"}]
    ["grains-kind/{grains-kind}"
     {:name :route/search-for-grains
      :parameters {:path {:grains-kind string?}
                   :query {(ds/opt :page-number) int?
                           (ds/opt :page-size) int?}}
      :doc ""
      :controllers [{:parameters {:path [:grains-kind]
                                  :query [:page-number :page-size]}
                     :start (fn [{{:keys [grains-kind]} :path
                                  {:keys [page-number page-size] :or {page-number 1 page-size 10}} :query}]
                              (dispatch [::events/store-grains-kind grains-kind :seeking])
                              (dispatch [::events/fetch-ads grains-kind (-m page-number page-size)])
                              (dispatch [::events/store-ads-pagination-info :seeking (-m page-number page-size)]))
                     :stop #(js/console.log "STOP: " "ad-type/:ad-type/grains-kind/:grains-kind")}]}]]
   ["thank-you"
    {:name :route/thank-you
     :doc "Thank you page"
     :controllers [{:identity identity}]}]
   ["error"
    {:name :route/error
     :doc "error page"
     :controllers [{:identity identity}]}]
   ["privacy-policy"
    {:name :route/privacy-policy
     :doc "privacy policy"
     :public? true
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
