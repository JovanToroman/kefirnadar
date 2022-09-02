(ns kefirnadar.application.handlers
  (:require [kefirnadar.configuration.client :as client]
            [kefirnadar.application.queries :as q]
            [ring.util.http-response :as r]
            [taoensso.timbre :refer [infof]]
            [datomic.client.api :as d])
  (:import (java.util Date)))

(def conn (client/get-conn))

(defn get-ads
  "This handler returns all active ads."
  [input]
  (let [ads (q/get-ads-by-kind-and-region (client/db) (:path-params input))]
    (if (not-empty ads)
      (r/ok ads)
      (r/bad-request {:status :error}))))

(defn create-user
  "Creates ad."
  [{:keys [parameters]}]
  (infof "Parameters: %s" parameters)
  (let [entity-body {:ad/firstname   (get-in parameters [:body :ad/firstname])
                     :ad/lastname    (get-in parameters [:body :ad/lastname])
                     :ad/region      (get-in parameters [:body :ad/region])
                     :ad/post        (get-in parameters [:body :ad/post] false)
                     :ad/pick-up     (get-in parameters [:body :ad/pick-up] false)
                     :ad/grains-kind (get-in parameters [:body :ad/grains-kind])
                     :ad/quantity    (get-in parameters [:body :ad/quantity])
                     :ad/created     (Date.)}
        assembled-entity-body (cond
                                (and (get-in parameters [:body :ad/phone-number])
                                     (get-in parameters [:body :ad/email])) (assoc entity-body
                                                                              :ad/phone-number (get-in parameters [:body :ad/phone-number])
                                                                              :ad/email (get-in parameters [:body :ad/email]))
                                (get-in parameters [:body :ad/phone-number]) (assoc entity-body
                                                                               :ad/phone-number (get-in parameters [:body :ad/phone-number]))
                                (get-in parameters [:body :ad/email]) (assoc entity-body
                                                                        :ad/email (get-in parameters [:body :ad/email])))
        result (q/add-entity! conn assembled-entity-body)
        new-ad-id (-> result :tempids vals first)
        db-after (:db-after result)
        new-ad (d/pull db-after [:*] new-ad-id)]
    (if (:db-after result)
      (r/ok new-ad)
      (r/bad-request))))

