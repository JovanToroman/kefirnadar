(ns user
  (:require [kefirnadar.configuration.server :as server]))

(defn start []
  (server/init-db!)
  (server/start-server))

(defn stop []
  (server/stop-server))

(defn restart []
  (stop)
  (server/reload-namespaces))