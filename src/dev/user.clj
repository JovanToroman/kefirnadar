(ns user
  (:require [kefirnadar.configuration.server :as server]))

(defn start []
  (server/start-server-and-db))

(defn stop []
  (server/stop-server))

(defn restart []
  (stop)
  (server/reload-namespaces))