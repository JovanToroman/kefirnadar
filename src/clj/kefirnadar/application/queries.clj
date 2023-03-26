(ns kefirnadar.application.queries
  (:require
    [kefirnadar.configuration.postgres :as postgres]
    [taoensso.timbre :as log]))

;; region queries
(defn get-expired-ads
  []
  (postgres/execute-transaction! {:select [:ad_id]
                                  :from [:ad]
                                  :where [:< :ad.created_on [:raw ["NOW() - INTERVAL '30 days'"]]]}))

(defn get-ads
  [{:keys [page-number page-size regions seeking-milk-type? seeking-water-type? seeking-kombucha? receive-by-post?
           receive-in-person?]}]
  (let [offset (* (dec page-number) page-size)
        where-clause (cond-> []
                       (seq regions) (conj [:in :ad.region regions])
                       (some boolean? [seeking-milk-type? seeking-water-type? seeking-kombucha?])
                       (conj (cond-> [:or]
                               (some? seeking-milk-type?) (conj [:= :ad.sharing_milk_type seeking-milk-type?])
                               (some? seeking-water-type?) (conj [:= :ad.sharing_water_type seeking-water-type?])
                               (some? seeking-kombucha?) (conj [:= :ad.sharing_kombucha seeking-kombucha?])))
                       (some boolean? [receive-by-post? receive-in-person?])
                       (conj (cond-> [:or]
                               (some? receive-by-post?) (conj [:= :ad.send_by_post receive-by-post?])
                               (some? receive-in-person?) (conj [:= :ad.share_in_person receive-in-person?]))))]
    (log/debug "Where clause: " where-clause)
    (log/spy :debug
      {:ads (postgres/execute-query!
              (cond-> {:select [:created_on :first_name :last_name :region :send_by_post :share_in_person
                                :quantity :phone_number :email :ad_id :sharing_milk_type :sharing_water_type
                                :sharing_kombucha]
                       :from [:ad]
                       :limit page-size
                       :offset offset
                       :order-by [[:ad.created_on :desc]]}
                (seq where-clause) (assoc :where (into [:and] where-clause))))
       :ads-count (:count (postgres/execute-one!
                            (cond-> {:select [:%count.*]
                                     :from [:ad]}
                              (seq where-clause) (assoc :where (into [:and] where-clause)))))})))
;; endregion

;; region transactions
(defn add-ad
  [ad-info]
  (log/info :debug "add-ad: " ad-info)
  (postgres/execute-transaction! {:insert-into [:ad]
                                  :columns (vec (keys ad-info))
                                  :values [(vec (vals ad-info))]}))

(defn remove-old-ads
  [ad-ids]
  (log/info :debug "remove-old-ads: " ad-ids)
  (postgres/execute-transaction! {:delete-from [:ad]
                                  :where [:in :ad.ad_id ad-ids]}))
;; endregion


(comment
  (mapv (fn [no]
          (add-ad {:send_by_post true,
                   :email "",
                   :first_name "asdasd",
                   :phone_number (str "062" no),
                   :region "Apatin",
                   :created_on [:now],
                   :last_name "asdassadsad",
                   ;;:user_id "10226481938492906",
                   :quantity 3,
                   :share_in_person false
                   :sharing_milk_type true}))
    (range 100000 100010)))

(comment "Remove all ads"
  (postgres/execute-transaction! {:truncate [:ad]}))
