(ns kefirnadar.application.queries
  (:require
    [datomic.client.api :as d]))


(defn user                                                  ;; currently, not used, maybe in future
  "Find a user based on its id."
  [db user-id]
  (d/pull db [:*] user-id))


(defn users
  ([db]
   (users db nil))
  ([db cond]
   (println "COND: " cond)
   (let [q (cond->
             '{:find  [(pull ?eid [*])]
               :where [[?eid :ad/created]]}
             (map? cond) (update :where into (map #(into ['?eid] %)) cond))]
     (map first (d/q q db)))))


(defn add-entity!
  "Transact one item if it is a map, or many items otherwise."
  [conn item]
  (d/transact conn {:tx-data (if (map? item) [item] item)}))
