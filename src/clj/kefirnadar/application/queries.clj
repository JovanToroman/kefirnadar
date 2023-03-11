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
  [grains-kind {:keys [page-number page-size]}]
  (let [offset (* (dec page-number) page-size)]
    (log/spy :debug
      (postgres/execute-query! {:select [:created_on :first_name :last_name :region :send_by_post :share_in_person
                                         :quantity :grains_kind :phone_number :email :ad_id]
                                :from [:ad]
                                :where [:= :ad.grains_kind grains-kind]
                                :limit page-size
                                :offset offset
                                :order-by [[:ad.created_on :desc]]}))))

(defn get-ads-count
  [grains-kind]
  (:count (postgres/execute-one! {:select [:%count.*]
                                  :from [:ad]
                                  :where [:= :ad.grains_kind grains-kind]})))
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
  (mapv (fn [no] (add-ad {:send_by_post true,
           :email "",
           :first_name "asdasd",
           :phone_number (str "062" no),
           :grains_kind "milk-type",
           :region "Ada",
           :created_on [:now],
           :last_name "asdassadsad",
           :user_id "10226481938492906",
           :quantity 3,
           :share_in_person false}))
    (range 100000 100100)))

(comment "Remove all ads"
  (postgres/execute-transaction! {:truncate [:ad]}))
