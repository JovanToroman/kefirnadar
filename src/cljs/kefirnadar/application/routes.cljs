(ns kefirnadar.application.routes
  (:require
    [kefirnadar.application.db :as db]
    [kefirnadar.application.subscriptions :as subs]
    [kefirnadar.application.auth :as auth]
    [kefirnadar.application.events :as events]
    [kefirnadar.common.coercion :as coerce-common]
    [kefirnadar.common.utils :refer-macros [-m]]
    [kefirnadar.common.specs :as specs-common]
    [re-frame.core :refer [dispatch subscribe]]
    [reitit.coercion :as coercion]
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
                      {:identity identity
                       :start (fn [] (auth/get-authentication-statuses!))}]
        :doc "Home page"}]
   ["sharing" {:name :route/sharing
               :doc "Form to share grains"}]
   ["seeking"
    {:name :route/seeking
     :doc "Page where users browse existing grains ads"
     :parameters {:query {(ds/opt :page-number) int?
                          (ds/opt :page-size) int?
                          (ds/opt :regions) ::specs-common/regions
                          (ds/opt :seeking-milk-type?) boolean?
                          (ds/opt :seeking-water-type?) boolean?
                          (ds/opt :seeking-kombucha?) boolean?
                          (ds/opt :receive-by-post?) boolean?
                          (ds/opt :receive-in-person?) boolean?}}
     :controllers [{:parameters {:query [:page-number :page-size :regions :seeking-milk-type? :seeking-water-type?
                                         :seeking-kombucha? :receive-by-post? :receive-in-person?]}
                    :start (fn [{{:keys [page-number page-size] :or {page-number 1 page-size 10} :as query} :query}]
                             (let [query (coerce-common/coerce-regions query)
                                   filters (select-keys query (keys (get-in db/default-db [:ads :seeking :filters])))]
                               (dispatch [::events/fetch-ads filters (-m page-number page-size)])
                               (dispatch [::events/update-filters filters])
                               (dispatch [::events/store-ads-pagination-info :seeking (-m page-number page-size)])))}]}]
   ["odjava"
    {:name :route/odjava
     :doc "Odjavi korisnika"
     :controllers [{:start (fn [_]
                             (auth/log-user-out (:facebook auth/auth-methods)))}]}]
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

(defonce router (rf/router routes {                         ;; `:compile` makes the router faster
                                   :compile coercion/compile-request-coercers}))
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
