(ns kefirnadar.application.routes
  (:require [clojure.spec.alpha :as s]
            [kefirnadar.application.handlers :as h]
            [kefirnadar.application.specs :as specs]
            [kefirnadar.common.specs :as specs-common]
            [kefirnadar.common.utils]
            [spec-tools.data-spec :as ds]))

(def routes
  ["/" {}
   ["" {:get {:handler h/pocetna
              :parameters {}}}]
   ["api" {:swagger {:consumes ["application/edn" "application/transit+json"]
                     :produces ["application/edn" "application/transit+json"]}}
    ["/auth"
     ["/potvrdi-fejsbuk-korisnika" {:post {:handler h/potvrdi-fejsbuk-korisnika
                                           :parameters {:body {:accessToken string?
                                                               :expiresIn number?
                                                               :signedRequest string?
                                                               :userID string?
                                                               :name string?
                                                               (ds/opt :email) string?}}}}]
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
                                 (ds/opt :receive-in-person?) boolean?
                                 (ds/opt :id-korisnika) int?}}}}]
    ["/oglas"
     ["/prikazi/{id-oglasa}" {:get {:handler h/dohvati-oglas
                                    :parameters {:path {:id-oglasa int?}}}}]
     ["/dodaj" {:post {:handler h/postavi-oglas
                       :parameters {:body {:korisnik/id (s/nilable int?)
                                           :ad/oblast string?
                                           :ad/slanje? (s/nilable boolean?)
                                           :ad/preuzimanje? (s/nilable boolean?)
                                           :ad/broj-telefona (s/nilable string?)
                                           :ad/imejl (s/nilable string?)
                                           :ad/deli-mlecni? (s/nilable boolean?)
                                           :ad/deli-vodeni? (s/nilable boolean?)
                                           :ad/deli-kombucu? (s/nilable boolean?)}}}}]
     ["/izbrisi/{id-oglasa}" {:delete {:handler h/izbrisi-oglas
                                       :parameters {:path {:id-oglasa int?}}}}]]
    ["/posalji-kontakt-poruku" {:post {:handler h/posalji-kontakt-poruku
                                       :parameters {:body {(ds/opt :imejl) string?
                                                           (ds/opt :poruka) string?}}}}]]])
