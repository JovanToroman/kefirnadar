(ns kefirnadar.configuration.server
  (:require
    [clojure.java.io :as io]
    [clojure.tools.cli :as cli]
    [clojure.tools.namespace.repl :as repl]
    [kefirnadar.application.async :as async]
    [kefirnadar.application.web :as web]
    [kefirnadar.configuration.config :as config]
    [kefirnadar.configuration.postgres :as postgres]
    [mount.core :as mount]
    [nrepl.server :as nrepl]
    [org.httpkit.server :as kit]
    [ring.middleware.reload :as reload]
    [ring.middleware.resource :refer [wrap-resource]]
    [taoensso.timbre :as log])
  (:gen-class)
  (:import (org.postgresql.util PSQLException)))


(defonce server (atom nil))

(def cli-options
  [["-p" "--profile PROFILE" "The profile to use" :default :dev :parse-fn keyword]])

(defn stop-server []
  (when (some? (postgres/datasource))
    (postgres/stop!))
  (when @server
    @(kit/server-stop! @server)                             ; we deref the returned promise to do the stopping in sync
    (reset! server nil)))

(defn start-server []
  (reset! server
    (kit/run-server (wrap-resource (reload/wrap-reload #'web/app) "/public")
      {:port 8088 :legacy-return-value? false})))

(defn init-db! []
  (try (postgres/start! (slurp (io/reader (io/resource "data/schema.sql"))))
       (log/info "System started")
       (catch PSQLException e (log/error
                                (str "DB failed to start. It seems you are missing your secrets file. Please "
                                  (format "make sure that file /etc/kefirnadar-%s-secrets.edn exists and that it "
                                    (:profile (mount/args)))
                                  "contains Postgresql config. Exception message: " (.getMessage e))))))

(defn start-server-and-db []
  (start-server)
  (init-db!))

(defn reload-namespaces []
  (repl/refresh :after 'kefirnadar.configuration.server/start-server-and-db))

(defn -main [& args]
  (let [opts (:options (cli/parse-opts args cli-options))]
    (log/info "Arguments: " opts)
    (mount/start-with-args opts))
  (log/set-level! :info)
  (log/info "Establishing DB connection")
  (init-db!)
  (log/info "Starting local server")
  (start-server)
  (log/info "Local server started on port 8088")
  (log/info "Starting NREPL server on port " @config/nrepl-port)
  (nrepl/start-server :port @config/nrepl-port)
  #_(async/remove-old-ads-thread true))
