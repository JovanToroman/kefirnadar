(ns kefirnadar.application.queries
  (:require
    [datomic.client.api :as d]))


(defn user                                                  ;;this will be essential later
  "Find a user based on its id."
  [db user-id]
  (d/pull db [:*] user-id))


(defn users                                                 ;;cond kao mapa
  ([db]
   (users db nil))
  ([db {:keys [cond]}]
   (let [q (cond->
             '{:find  [(pull ?eid [*])]
               :where [[?eid :user/created]]}
             (map? cond) (concat (map (partial cons '?eid) (vec cond))))]
     (map first (d/q q db)))))

#_(defn users
    ([db]
     (users db nil))
    ([db {:keys [cond]}]
     (let [q (cond->
               '{:find  [(pull ?eid [*])]
                 :where [[?eid :user/created] #_[?eid :user/region :Ada] #_[?eid :user/firstname "Nedja"]]}
               (map? cond) (concat (map (partial cons '?eid) (vec {:user/firstname "Nedja"}))))]
       (map first (d/q q db)))))


(defn add-entity!
  "Transact one item if it is a map, or many items otherwise."
  [conn item]
  (d/transact conn {:tx-data (if (map? item) [item] item)}))

;;test region

(let [cond {:user/firstname "Nedja"}
      q (cond->
          '{:find  [(pull ?eid [*])]
            :where [[?eid :user/created] #_[?eid :user/region :Ada] #_[?eid :user/firstname "Nedja"]]}
          (map? cond) (concat (map (partial cons '?eid) cond)))]
  q)

;; ([:find [(pull ?eid [*])]] [:where [[?eid :user/created]]] (?eid :user/firstname "Nedja")) LOSA POVRATNA VREDNOST
;; {:find [(pull ?eid [*])] :where [[?eid :user/created] [?eid :user/firstname "Nedja"]]} DOBRA POVRATNA VREDNOST