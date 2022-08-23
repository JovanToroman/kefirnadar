(ns kefirnadar.configuration.client
  (:require [datomic.client.api :as d]))


(def db-name "kefirnadar")


(def cfg {:server-type        :peer-server
          :access-key         "kefirnadar"                  ; TODO: change to something more complicated
          :secret             "kefirnadar"                  ; TODO: change to something more complicated
          :endpoint           "localhost:8998"
          :validate-hostnames false})


(defn get-client
  "Creates a datomic peer server client based on default configuration."
  []
  (try
    (d/client cfg)
    (catch Exception e (println (ex-data e)))))

(defn get-conn*
  "Return a new connection to datomic db."
  []
  (d/connect (get-client) {:db-name db-name}))

(def get-conn (memoize get-conn*))

(defn db
  "Returns the most recent database from the connection."
  []
  (d/db (get-conn)))
