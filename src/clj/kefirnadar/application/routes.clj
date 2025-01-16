(ns kefirnadar.application.routes
  (:require [kefirnadar.application.handlers :as h]
            [spec-tools.data-spec :as ds]
            [clojure.spec.alpha :as s]
            [kefirnadar.application.specs :as specs]
            [kefirnadar.common.specs :as specs-common]
            [kefirnadar.common.utils]))

(def routes
  ["/api" {:swagger {:consumes ["application/edn" "application/transit+json"]
                     :produces ["application/edn" "application/transit+json"]}}
   ["/auth"
    ["/potvrdi-fejsbuk-korisnika" {:post {:handler h/potvrdi-fejsbuk-korisnika
                                          :parameters {:body {:accessToken string?
                                                              :expiresIn number?
                                                              :signedRequest string?
                                                              :userID string?
                                                              :name string?}}}}]
    ["/dodaj-korisnika" {:post {:handler h/dodaj-korisnika
                                :parameters {:body {:imejl string?
                                                    :lozinka string?
                                                    :korisnicko-ime string?}}}}]
    ["/prijava" {:post {:handler h/prijava
                        :parameters {:body {:imejl string?
                                            :lozinka string?}}}}]
    ["/aktiviraj-korisnika" {:post {:handler h/aktiviraj-korisnika
                                    :parameters {:body {:aktivacioni-kod string?}}}}]
    ["/posalji-aktivacioni-kod" {:post {:handler h/posalji-aktivacioni-kod
                                        :parameters {:body {:imejl string?}}}}]
    ["/posalji-imejl-za-resetovanje-lozinke" {:post {:handler h/posalji-imejl-za-resetovanje-lozinke
                                                     :parameters {:body {:imejl string?}}}}]
    ["/resetuj-lozinku" {:post {:handler h/resetuj-lozinku
                                :parameters {:body {:kod-za-resetovanje-lozinke string?
                                                    :nova-lozinka string?}}}}]]
   ["/oglasi"
    {:get {:handler h/dohvati-oglase
           :parameters {:query {(ds/opt :page-number) pos-int?
                                (ds/opt :page-size) ::specs/page-size
                                (ds/opt :regions) ::specs-common/regions
                                (ds/opt :seeking-milk-type?) boolean?
                                (ds/opt :seeking-water-type?) boolean?
                                (ds/opt :seeking-kombucha?) boolean?
                                (ds/opt :receive-by-post?) boolean?
                                (ds/opt :receive-in-person?) boolean?}}}}]
   ["/oglas"
    ["/dodaj" {:post {:handler h/postavi-oglas
                      :parameters {:body {:korisnik/id (s/nilable int?)
                                          :ad/region string?
                                          :ad/post? boolean?
                                          :ad/pick-up? boolean?
                                          :ad/quantity number?
                                          (ds/opt :ad/phone-number) string?
                                          (ds/opt :ad/email) string?
                                          :ad/sharing-milk-type? boolean?
                                          :ad/sharing-water-type? boolean?
                                          :ad/sharing-kombucha? boolean?}}}}]]
   ["/posalji-kontakt-poruku" {:post {:handler h/posalji-kontakt-poruku
                                      :parameters {:body {(ds/opt :imejl) string?
                                                          (ds/opt :poruka) string?}}}}]])
