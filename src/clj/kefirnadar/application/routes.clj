(ns kefirnadar.application.routes
  (:require [kefirnadar.application.handlers :as h]
            [spec-tools.data-spec :as ds]))

(def routes
  ["" {:swagger {:consumes ["application/edn" "application/transit+json"]
                 :produces ["application/edn" "application/transit+json"]}}
   ["/list/grains-kind/{ad/grains-kind}/region/{ad/region}"
    {:get {:handler    h/get-ads
           :parameters {:path {:ad/region      keyword?
                               :ad/grains-kind keyword?}}}}]
   ["/create" {:post {:handler    h/create-user
                      :parameters {:body {:ad/firstname             string?
                                          :ad/lastname              string?
                                          :ad/region                keyword?
                                          :ad/post                  boolean?
                                          :ad/pick-up               boolean?
                                          :ad/quantity              number?
                                          :ad/grains-kind           keyword?
                                          (ds/opt :ad/phone-number) string?
                                          (ds/opt :ad/email)        string?}}}}]
   ["/post-example" {:post {:handler h/get-ads}}]
   ["/example-delete" {:delete {:handler    (fn [] "implement me!")
                                :parameters {:body {:db/id number?}}}}]])