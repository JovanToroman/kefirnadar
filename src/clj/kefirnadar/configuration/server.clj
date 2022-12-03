(ns kefirnadar.configuration.server
  (:require [kefirnadar.application.async :as async]
            [kefirnadar.application.web :as web]
            [kefirnadar.configuration.client :as client]
            [kefirnadar.configuration.start :as start]
            [org.httpkit.server :as kit]
            [ring.middleware.reload :as reload]
            [taoensso.timbre :as log]))


(def server (atom nil))

(defn stop-server []
  (when @server
    (@server)
    (reset! server nil)))

(defn start-server []
  (stop-server)
  (reset! server
          (kit/run-server (reload/wrap-reload #'web/app) {:port 8080})))

(defn -main [& _args]
  (log/info "Establishing DB connection")
  (start/dev-start)
  (log/infof "Db name: %s" client/db-name)
  (log/info "Starting local server")
  (start-server)
  (log/info "Local server started on port 8080")
  (async/retract-old-ads-thread true))
