(ns kefirnadar.application.routes)

(def routes
  ["" {:swagger {:consumes ["application/edn" "application/transit+json"]
                 :produces ["application/edn" "application/transit+json"]}}
   ["/example-get" {:get {:handler (fn [] "implement me!")}}]
   ["/example-put" {:put {:handler (fn [] "implement me!")
                          :parameters {:body {:todo/title string?}}}}]
   ["/example-post" {:post {:handler (fn [] "implement me!")
                            :parameters {:body {:data map?}}}}]
   ["/example-delete" {:delete {:handler (fn [] "implement me!")
                                :parameters {:body {:db/id number?}}}}]])