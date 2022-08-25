(ns kefirnadar.application.queries
  (:require
    [datomic.client.api :as d]))


(defn ad                                                  ;; currently, not used, maybe in future
  "Find an ad based on its id."
  [db ad-id]
  (d/pull db [:*] ad-id))

(defn ads
  ([db]
   (ads db nil))
  ([db cond]
   (println "COND: " cond)
   (let [condition-vector (mapv (fn [cond-pair]
                                  ['?eid (key cond-pair) (val cond-pair)]) cond)
         q (reduce conj '[:find (pull ?eid [*])
                          :where [?eid :ad/created]] condition-vector)]
     (map first (d/q q db)))))

(defn add-entity!
  "Transact one item if it is a map, or many items otherwise."
  [conn item]
  (d/transact conn {:tx-data (if (map? item) [item] item)}))
