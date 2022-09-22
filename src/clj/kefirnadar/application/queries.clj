(ns kefirnadar.application.queries
  (:require
    [datomic.client.api :as d]))


(def query-by-kind-and-region
  '[:find (pull ?eid [*])
    :in $ ?grains-kind ?region
    :where
    [?eid :ad/region ?region]
    [?eid :ad/grains-kind ?grains-kind]])


(def query-old-ad-ids
  '[:find (pull ?e [:db/id])
    :in $ ?last-month
    :where [?e :ad/created ?created]
    [(< ?created ?last-month)]])


(defn get-ads-by-kind-and-region
  ([db]
   (get-ads-by-kind-and-region db nil))
  ([db {:ad/keys [grains-kind region]}]
   "This function accepts a condition and retrieves ads based on the condition values."
   (map first (d/q query-by-kind-and-region db (keyword grains-kind) (keyword region)))))


(defn add-entity!
  "Transact one item if it is a map, or many items otherwise."
  [conn item]
  (d/transact conn {:tx-data (if (map? item) [item] item)}))
