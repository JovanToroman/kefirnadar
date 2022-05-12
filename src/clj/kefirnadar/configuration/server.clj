(ns kefirnadar.configuration.server
  (:require [kefirnadar.application.web :as web]
            [kefirnadar.configuration.client :as client]
            [kefirnadar.configuration.start :as start]
            [org.httpkit.server :as kit]
            [ring.middleware.reload :as reload]
            [taoensso.timbre :refer [infof]]))

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
  (infof "Establishing DB connection")
  (start/dev-start)
  (infof "Db name: %s" (:db-name (client/db)))
  (infof "Starting local server")
  (start-server)
  (infof "Local server started on port 8080"))
