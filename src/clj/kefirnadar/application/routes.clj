(ns kefirnadar.application.routes
  (:require [kefirnadar.application.handlers :as h]
            [spec-tools.data-spec :as ds]))

(def routes
  ["" {:swagger {:consumes ["application/edn" "application/transit+json"]
                 :produces ["application/edn" "application/transit+json"]}}
   ["/list/grains-kind/{ad/grains-kind}/region/{ad/region}"
    {:get {:handler h/get-ads
           :parameters {:path {:ad/region string?
                               :ad/grains-kind string?}}}}]
   ["/create" {:post {:handler h/create-ad
                      :parameters {:body {:ad/firstname string?
                                          :ad/lastname string?
                                          :ad/region string?
                                          (ds/opt :ad/post?) boolean?
                                          (ds/opt :ad/pick-up?) boolean?
                                          :ad/quantity number?
                                          :ad/grains-kind string?
                                          (ds/opt :ad/phone-number) string?
                                          (ds/opt :ad/email) string?
                                          :user-id string?}}}}]])