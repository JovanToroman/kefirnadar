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
  ["/" {:coercion reitit.coercion.spec/coercion
        :controllers [{:identity identity
                       :start #(dispatch [::auth/ucitaj-korisnika])}]}
   ["" {:name :route/home
        :controllers [{:identity identity
                       :start #(dispatch [::events/clean-db])}]
        :doc "Home page"}]
   ["delim" {:name :route/delim}]
   ["tražim"
    {:name :route/trazim
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
   ["moji-oglasi"
    {:name :route/moji-oglasi
     :controllers [{:start (fn []
                             (dispatch [::events/moji-oglasi]))}]}]
   ["odjava"
    {:name :route/odjava
     :doc "Odjava korisnika"
     :controllers [{:start (fn [_]
                             (dispatch [::auth/odjavi-korisnika]))}]}]
   ["registracija"
    {:name :route/registracija
     :doc "Registracija korisnika"
     :public? true}
    ;; TODO: mozda dodati da se prekopira imejl adresa ako je korisnik dosao sa forme za prijavu kako bi se olaksalo korisniku
    ]
   ["nakon-registracije"
    {:name :route/nakon-registracije
     :doc "Obaveštenja za novoregistrovanog korisnika o potrebnoj aktivaciji naloga"
     :public? true}]
   ["prijava"
    {:name :route/prijava
     :doc "Prijava korisnika"
     :public? true}]
   ["aktiviraj-korisnika/{aktivacioni-kod}"
    {:name :route/aktiviraj-korisnika
     :doc "Ruta za aktiviranje novokreiranog korisničkog naloga"
     :controllers [{:parameters {:path [:aktivacioni-kod]}
                    :start (fn [{{:keys [aktivacioni-kod]} :path}]
                             (dispatch [::auth/aktiviraj-korisnika aktivacioni-kod]))}]
     :public? true}]
   ["slanje-imejla-za-resetovanje-lozinke"
    {:name :route/slanje-imejla-za-resetovanje-lozinke
     :doc "Ruta za resetovanje lozinke"
     :controllers [{:parameters {:query [:imejl]}
                    :start (fn [{{:keys [imejl]} :query}]
                             (dispatch [::events/azuriraj-formu-za-slanje-imejla-za-resetovanje-lozinke :imejl imejl]))}]
     :public? true}]
   ["nakon-slanja-imejla-za-resetovanje-lozinke"
    {:name :route/nakon-slanja-imejla-za-resetovanje-lozinke
     :doc "Obaveštenje za korisnika o resetovanju lozinke"
     :public? true}]
   ["resetovanje-lozinke/{kod-za-resetovanje-lozinke}"
    {:name :route/resetovanje-lozinke
     :doc "Ruta za aktiviranje novokreiranog korisničkog naloga"
     :controllers [{:parameters {:path [:kod-za-resetovanje-lozinke]}
                    :start (fn [{{:keys [kod-za-resetovanje-lozinke]} :path}]
                             (dispatch [::events/azuriraj-formu-za-resetovanje-lozinke
                                        :kod-za-resetovanje-lozinke kod-za-resetovanje-lozinke]))}]
     :public? true}]
   ["nakon-resetovanja-lozinke"
    {:name :route/nakon-resetovanja-lozinke
     :doc "Obaveštenje o uspešnom resetovanju lozinke"
     :public? true}]
   ["kontakt"
    {:name :route/kontakt
     :public? true}]
   ["nakon-slanja-kontakt-poruke"
    {:name :route/nakon-slanja-kontakt-poruke
     :doc "Obaveštenje o uspešnom slanju kontakt poruke"
     :public? true}]
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
