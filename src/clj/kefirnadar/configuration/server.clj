(ns kefirnadar.configuration.server
  (:require
    [clojure.java.io :as io]
    [kefirnadar.application.async :as async]
    [kefirnadar.application.web :as web]
    [kefirnadar.configuration.postgres :as postgres]
    [clojure.tools.namespace.repl :as repl]
    [org.httpkit.server :as kit]
    [ring.middleware.reload :as reload]
    [taoensso.timbre :as log])
  (:gen-class)
  (:import (org.postgresql.util PSQLException)))


(defonce server (atom nil))

(repl/set-refresh-dirs "src/clj/kefirnadar/application")

(defn stop-server []
  (when @server
    @(kit/server-stop! @server)                             ; we deref the returned promise to do the stopping in sync
    (reset! server nil)))

(defn start-server []
  (stop-server)
  (reset! server
    (kit/run-server (reload/wrap-reload #'web/app) {:port 8088 :legacy-return-value? false})))

(defn init-db! []
  (try (postgres/start! (slurp (io/reader (io/resource "data/schema.sql"))))
       (log/info "System started")
       (catch PSQLException _ (log/error (str "DB failed to start. It seems you are missing your secrets file. Please "
                                           "make sure that file /etc/kefirnadar-dev-secrets.edn exists and that it "
                                           "contains Postgresql config.")))))

(defn reload-namespaces []
  (repl/refresh :after 'kefirnadar.configuration.server/start-server))

(defn -main [& _args]
  (log/info "Establishing DB connection")
  (init-db!)
  (log/info "Starting local server")
  (start-server)
  (log/info "Local server started on port 8080")
  (async/remove-old-ads-thread true))
