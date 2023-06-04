(ns kefirnadar.application.routes
  (:require [kefirnadar.application.handlers :as h]
            [spec-tools.data-spec :as ds]
            [kefirnadar.application.specs :as specs]
            [kefirnadar.common.specs :as specs-common]
            [kefirnadar.common.utils]))

(def routes
  ["/api" {:swagger {:consumes ["application/edn" "application/transit+json"]
                     :produces ["application/edn" "application/transit+json"]}}
   ["/auth/potvrdi-fejsbuk-korisnika"
    {:post {:handler h/potvrdi-fejsbuk-korisnika
            :parameters {:body {:accessToken string?
                                :expiresIn number?
                                :signedRequest string?
                                :userID string?
                                :name string?}}}}]
   ["/list"
    {:get {:handler h/get-ads
           :parameters {:query {(ds/opt :page-number) pos-int?
                                (ds/opt :page-size) ::specs/page-size
                                (ds/opt :regions) ::specs-common/regions
                                (ds/opt :seeking-milk-type?) boolean?
                                (ds/opt :seeking-water-type?) boolean?
                                (ds/opt :seeking-kombucha?) boolean?
                                (ds/opt :receive-by-post?) boolean?
                                (ds/opt :receive-in-person?) boolean?}}}}]
   ["/create" {:post {:handler h/create-ad
                      :parameters {:body {:korisnik/id int?
                                          :ad/region string?
                                          :ad/post? boolean?
                                          :ad/pick-up? boolean?
                                          :ad/quantity number?
                                          (ds/opt :ad/phone-number) string?
                                          (ds/opt :ad/email) string?
                                          :ad/sharing-milk-type? boolean?
                                          :ad/sharing-water-type? boolean?
                                          :ad/sharing-kombucha? boolean?
                                          #_#_:user-id string?}}}}]])
