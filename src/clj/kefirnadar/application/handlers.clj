(ns kefirnadar.application.handlers
  (:require [kefirnadar.application.queries :as q]
            [kefirnadar.configuration.client :as client]
            [ring.util.http-response :as r]
            [datomic.client.api :as d]
            [taoensso.timbre :refer [infof]])
  (:import [java.util Date]))

(defn get-ads
  "This handler returns all active ads."
  [input]
  (println (:params input))
  (let [ads (q/get-ads-by-kind-and-region (client/db) (:path-params input))]
    (r/ok ads)))

(defn create-ad
  "Creates ad."
  [{:keys [parameters]}]
  (let [entity-body {:ad/firstname (get-in parameters [:body :ad/firstname])
                     :ad/lastname (get-in parameters [:body :ad/lastname])
                     :ad/region (get-in parameters [:body :ad/region])
                     :ad/post? (get-in parameters [:body :ad/post?] false)
                     :ad/pick-up? (get-in parameters [:body :ad/pick-up?] false)
                     :ad/grains-kind (get-in parameters [:body :ad/grains-kind])
                     :ad/quantity (get-in parameters [:body :ad/quantity])
                     :ad/created (Date.)}
        assembled-entity-body (cond
                                (and (get-in parameters [:body :ad/phone-number])
                                     (get-in parameters [:body :ad/email])) (assoc entity-body
                                                                              :ad/phone-number (get-in parameters [:body :ad/phone-number])
                                                                              :ad/email (get-in parameters [:body :ad/email]))
                                (get-in parameters [:body :ad/phone-number]) (assoc entity-body
                                                                               :ad/phone-number (get-in parameters [:body :ad/phone-number]))
                                (get-in parameters [:body :ad/email]) (assoc entity-body
                                                                        :ad/email (get-in parameters [:body :ad/email])))
        result (q/add-entity! (client/get-conn) assembled-entity-body)
        new-ad-id (-> result :tempids vals first)
        db-after (:db-after result)
        new-ad (d/pull db-after [:*] new-ad-id)]
    (if (:db-after result)
      (r/ok new-ad)
      (r/bad-request))))

