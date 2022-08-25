(ns kefirnadar.application.async
  (:require [kefirnadar.configuration.client :as client]
            [datomic.client.api :as d]
            [taoensso.timbre :as log])
  (:import (java.util Date)
           (java.time Instant Duration)))


(defn assemble-data-for-retraction [ids]
  "Assemble required data structure from ids for 'tx-retract-old-ids' .
    [[:db/retractEntity id] [:db/add 'datomic.tx' :db/doc 'remove old ad']]"
  (reduce into [] (mapv (fn [id]
                          [[:db/retractEntity (:db/id id)] [:db/add "datomic.tx" :db/doc "remove old ad"]])
                          (flatten ids))))


(defn tx-retract-old-ads [conn data]
  "Transact function for retracting ads from database."
  (d/transact
    conn
    {:tx-data (assemble-data-for-retraction data)}))


(defn retract-old-ads-thread [running?]
    "Check for ads older then predefined time, retrieve ids and feed them to a tx-retract-old-ads function."
    (Thread.
      ^Runnable
      (while running?
        (try
          (let [conn (client/get-conn)
                db (client/db)
                last-month (Date/from (.minus (Instant/now) (Duration/ofDays 30)))
                last-minute (Date/from (.minus (Instant/now) (Duration/ofMinutes 1)))
                old-ad-ids (d/q '[:find (pull ?e pattern)
                                             :in $ ?last-month pattern
                                             :where [?e :ad/created ?created]
                                             [(< ?created ?last-month)]]
                                           db last-minute [:db/id])]
            (if (empty? old-ad-ids)
              (log/info "No ads older then 30 days to retract.")
              (tx-retract-old-ads conn old-ad-ids)))
          (catch Exception e
            (log/info "Exception in retract-old-ads-thread:" e)))
        (Thread/sleep 30000))))