(ns kefirnadar.application.routes
  (:require [kefirnadar.application.handlers :as h]))

(def routes
  ["" {:swagger {:consumes ["application/edn" "application/transit+json"]
                 :produces ["application/edn" "application/transit+json"]}}
   ["/list" {:get {:handler h/get-users}}]
   ["/create" {:post {:handler    h/create-user
                      :parameters {:body {:user/firstname   string?
                                          :user/lastname    string?
                                          :user/region      keyword?
                                          :user/post        boolean?
                                          :user/pick-up     boolean?
                                          :user/quantity    number?
                                          :user/grains-kind keyword?}}}}]
   ["/post-example" {:post {:handler h/get-users}}]
   ["/example-delete" {:delete {:handler    (fn [] "implement me!")
                                :parameters {:body {:db/id number?}}}}]])