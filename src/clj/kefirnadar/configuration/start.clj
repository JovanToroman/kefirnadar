(ns kefirnadar.configuration.start
  (:require [clojure.java.io :as io]
            [datomic.client.api :as d]
            [kefirnadar.configuration.client :as client]))

(def schema (read-string (slurp (io/reader (io/resource "data/schema.edn")))))
(def initial-values (read-string (slurp (io/reader (io/resource "data/initial.edn")))))

(defn dev-start []
  (let [conn (client/get-conn)]
    (d/transact conn {:tx-data schema})
    (d/transact conn {:tx-data initial-values})
    (println "System started")))

(defn stop []
  (println "System stopped"))

(defn dev-restart []
  (stop)
  (dev-start))

