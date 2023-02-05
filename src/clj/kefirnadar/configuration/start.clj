(ns kefirnadar.configuration.start
  (:require [clojure.java.io :as io]
            [datomic.client.api :as d]
            [kefirnadar.configuration.client :as client]
            [clojure.tools.namespace.repl :refer [refresh]]
            [taoensso.timbre :as log]))


(defn dev-start []
  (d/create-database (client/get-client) {:db-name client/db-name})
  (let [conn (client/get-conn)]
    (d/transact conn {:tx-data (read-string (slurp (io/reader (io/resource "data/schema.edn"))))})
    (d/transact conn {:tx-data (read-string (slurp (io/reader (io/resource "data/initial.edn"))))})
    (log/info "System started")))

(defn stop []
  (println "System stopped"))

(defn dev-restart []
  (stop)
  (refresh :after 'kefirnadar.configuration.start/dev-start))

