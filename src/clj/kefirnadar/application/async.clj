(ns kefirnadar.application.async
  (:require [kefirnadar.configuration.client :as client]
            [clj-time.core :as t]
            [datomic.client.api :as d]
            [taoensso.timbre :refer [infof]]
            [clojure.core.reducers :as cred])
  (:import (java.util Date)
           (java.time Instant Duration)))


(defn tx-retract-ad-component [id]
  [:db/retractEntity (first id)])

;; Pretrci svaki element i pored svakog elementa stavi element.
(defn tx-retract-ad-vector [collection]
  (let [a (mapv #(tx-retract-ad-component (vals %)) collection)]
    (loop [x (first a)
           y [:db/add "datomic.tx" :db/doc "remove old ad"]
           rem (rest a)
           final-vec []]
      (if (empty? rem)
        (conj final-vec x y)
        (recur (second (rest a)) y (rest rem) (conj final-vec x y))))))

(defn tx-retraction-operation [conn data]
  (d/transact
    conn
    {:tx-data (tx-retract-ad-vector data)}))


(defn db-clean-up-thread [running?]
  (Thread.
    (while running?
      (try
        (let [conn (client/get-conn)
              db (client/db)
              last-month (Date/from (.minus (Instant/now) (Duration/ofDays 30)))
              test (Date/from (.minus (Instant/now) (Duration/ofMinutes 1)))
              ids (d/q '{:find  [(pull ?e [:db/id])]
                         :in    [$ ?last-month]
                         :where [[?e :user/created _ ?tx]
                                 [?tx :db/txInstant ?created]
                                 [(< ?created ?last-month)]]}
                       db test)]
          (let [collection (into [] (cred/flatten ids))]
            (cond
              (> (count collection) 0) (tx-retraction-operation conn collection)
              (= (count collection) 0) (println "Collection is empty"))))
        (catch Exception e
          (println "Exception in Periodically Cleaning Database:" e)))
      (Thread/sleep 10000))))

