(ns kefirnadar.application.queries
  (:require
    [kefirnadar.configuration.postgres :as postgres]
    [taoensso.timbre :as log]))

;; region queries
(defn get-expired-ads
  []
  (postgres/execute-transaction! {:select [:ad_id]
                                  :from [:ad]
                                  :where [:< :created_at [:raw ["NOW() - INTERVAL '30 days'"]]]}))

(defn get-ads
  [grains-kind region]
  (log/spy :debug
    (postgres/execute-query! {:select [:created_on :first_name :last_name :region :send_by_post :share_in_person
                                       :quantity :grains_kind :phone_number :email]
                              :from [:ad]
                              :where [:and
                                      [:= :ad.grains_kind grains-kind]
                                      [:= :ad.region region]]})))
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
  (add-ad {:send_by_post true,
           :email "",
           :first_name "asdasd",
           :phone_number "062333444",
           :grains_kind "milk-type",
           :region "Ada",
           :created_on [:now],
           :last_name "asdassadsad",
           :user_id "10226481938492906",
           :quantity 3,
           :share_in_person false}))