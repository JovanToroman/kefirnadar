(ns kefirnadar.application.routes
  (:require [kefirnadar.application.handlers :as h]
            [spec-tools.data-spec :as ds]
            [kefirnadar.application.specs :as specs]
            [kefirnadar.common.utils]))

(def routes
  ["/api" {:swagger {:consumes ["application/edn" "application/transit+json"]
                     :produces ["application/edn" "application/transit+json"]}}
   ["/list/grains-kind/{ad/grains-kind}"
    {:get {:handler h/get-ads
           :parameters {:path {:ad/grains-kind string?}
                        :query {(ds/opt :page-number) pos-int?
                                (ds/opt :page-size) ::specs/page-size}}}}]
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
                                          #_#_:user-id string?}}}}]])