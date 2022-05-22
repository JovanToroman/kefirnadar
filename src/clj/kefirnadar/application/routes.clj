(ns kefirnadar.application.routes
  (:require [kefirnadar.application.handlers :as h]))

(def routes
  ["" {:swagger {:consumes ["application/edn" "application/transit+json"]
                 :produces ["application/edn" "application/transit+json"]}}
   ["/list" {:get {:handler h/get-users}}]
   ["/example-put" {:put {:handler (fn [] "implement me!")
                          :parameters {:body {:todo/title string?}}}}]
   ["/example-post" {:post {:handler (fn [] "implement me!")
                            :parameters {:body {:data map?}}}}]
   ["/example-delete" {:delete {:handler (fn [] "implement me!")
                                :parameters {:body {:db/id number?}}}}]])