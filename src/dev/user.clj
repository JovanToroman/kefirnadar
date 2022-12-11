(ns user
  (:require [kefirnadar.configuration.start :as start]))

(defn start []
  (start/dev-start))

(defn stop []
  (start/stop))

(defn restart []
  (start/dev-restart))