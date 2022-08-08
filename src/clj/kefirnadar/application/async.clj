(ns kefirnadar.application.async
  (:require [kefirnadar.configuration.client :as client]
            [clj-time.core :as t]
            [datomic.client.api :as d]
            [taoensso.timbre :refer [infof]]
            [clojure.core.reducers :as cred])
  (:import (java.util Date)
           (java.time Instant Duration)))


(defn db-clean-up-thread [running?]
  (Thread.
    (while running?
      (try
        (let [conn (client/get-conn)
              db (client/db)
              last-month (Date/from (.minus (Instant/now) (Duration/ofDays 30)))
              test (Date/from (.minus (Instant/now) (Duration/ofMinutes 2)))
              ids (d/q '{:find  [(pull ?e [:db/id])]
                         :in    [$ ?last-month]
                         :where [[?e :user/created _ ?tx]
                                 [?tx :db/txInstant ?created]
                                 [(< ?created ?last-month)]]}
                       db test)]
          #_(println (into [] (cred/flatten ids)))
          (let [collection (into {} (map (fn [x] x) (into [] (cred/flatten ids))))]
            (cond
              (> (count collection) 0) (println collection)
              (< (count collection) 0) (println "Collection is empty")))
          #_(let [collection (into [] (map (fn [x] [:db/retractEntity x]) (into [] (cred/flatten ids))))]
              (cond
              (> (count collection) 0) (d/transact conn {:tx-data [collection]})
              (= (count collection) 0) (println "There are no users to retract"))))
        (catch Exception e
          (println "Exception in Periodically Cleaning Database:" e)))
      (Thread/sleep 10000))))

