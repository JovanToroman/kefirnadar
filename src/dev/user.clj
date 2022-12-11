(ns user
  (:require [kefirnadar.configuration.start :as start]
            [kefirnadar.configuration.server :as server]))

(defn start []
  (start/dev-start)
  (server/start-server))

(defn stop []
  (start/stop)
  (server/stop-server))

(defn restart []
  (start/dev-restart))