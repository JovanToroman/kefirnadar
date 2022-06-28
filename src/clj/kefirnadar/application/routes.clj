(ns kefirnadar.application.routes
  (:require [kefirnadar.application.handlers :as h]
            [spec-tools.data-spec :as ds]))

(def routes
  ["" {:swagger {:consumes ["application/edn" "application/transit+json"]
                 :produces ["application/edn" "application/transit+json"]}}
   ["/list/grains-kind/{user/grains-kind}/region/{user/region}"
    {:get {:handler    h/get-users
           :parameters {:path {:user/region      keyword?
                               :user/grains-kind keyword?}}}}]
   ["/create" {:post {:handler    h/create-user
                      :parameters {:body {:user/firstname    string?
                                          :user/lastname     string?
                                          :user/region       keyword?
                                          :user/post         boolean?
                                          :user/pick-up      boolean?
                                          :user/quantity     number?
                                          :user/grains-kind  keyword?
                                          :user/phone-number string?
                                          :user/email        string?}}}}]
   ["/post-example" {:post {:handler h/get-users}}]
   ["/example-delete" {:delete {:handler    (fn [] "implement me!")
                                :parameters {:body {:db/id number?}}}}]])