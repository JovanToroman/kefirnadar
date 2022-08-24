(ns kefirnadar.application.async
  (:require [kefirnadar.configuration.client :as client]
            [datomic.client.api :as d]
            [taoensso.timbre :as log]
            [clojure.core.reducers :as reducers])
  (:import (java.util Date)
           (java.time Instant Duration)))


;; Stari kod:
#_(defn tx-retract-ad-component [id]
    [:db/retractEntity (first id)])

#_(defn tx-retract-ad-vector [collection]
    (let [a (mapv #(tx-retract-ad-component (vals %)) collection)]
      (loop [x (first a)
             y [:db/add "datomic.tx" :db/doc "remove old ad"]
             rem (rest a)
             final-vec []]
        (if (empty? rem)
          (conj final-vec x y)
          (recur (second (rest a)) y (rest rem) (conj final-vec x y))))))

;; Novi kod - kapiram kako interleave funkcionise i kako dobijamo zeljeni rezultat... morao sam mini promene da napravim.
(defn assemble-data-for-retraction [ids]
  "Assemble required data structure from ids for 'tx-retract-old-ids' .
    [[:db/retractEntity id] [:db/add 'datomic.tx' :db/doc 'remove old ad']]"
  (let [retract-entity-ids (mapv (fn [id]
                                   [:db/retractEntity (first (vals id))]) ids)]
    (vec
      (interleave
        retract-entity-ids
        (repeat (count retract-entity-ids) [:db/add "datomic.tx" :db/doc "remove old ad"])))))


(defn tx-retract-old-ads [conn data]
  "Transact function for retracting ads from database."
  (d/transact
    conn
    {:tx-data (assemble-data-for-retraction data)}))

(defn retract-old-ads-thread [running?]
  "Every 24h check for ads older then 30 days, retrieve ids and feed them to a tx-retract-old-ads function."
  (Thread.
    (while running?
      (try
        (let [conn (client/get-conn)
              db (client/db)
              last-month (Date/from (.minus (Instant/now) (Duration/ofDays 30)))
              last-minute (Date/from (.minus (Instant/now) (Duration/ofMinutes 1)))
              old-ad-ids (into [] (reducers/flatten (d/q '{:find [(pull ?e [:db/id])]
                                                       :in       [$ ?last-month]
                                                       :where    [[?e :ad/created ?created]
                                                               [(< ?created ?last-month)]]}
                                                         db last-minute)))]
          (if (empty? old-ad-ids)
            (log/info "No ads older then 30 days to retract.")
            (tx-retract-old-ads conn old-ad-ids)))
        (catch Exception e
          (log/info "Exception in retract-old-ads-thread:" e)))
      (Thread/sleep 10000))))
