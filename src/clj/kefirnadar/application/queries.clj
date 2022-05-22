(ns kefirnadar.application.queries
  (:require
    [datomic.client.api :as d]))

(defn users
  ([db]
   (users db nil))
  ([db {:keys [cond]}]
   (let [q (cond->
             '{:find [(pull ?eid [*])]
               :where [[?eid :user/created]]}
             (map? cond) (concat (map (partial cons '?eid) (vec cond))))]
     (map first (d/q q db)))))
