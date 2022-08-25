(ns kefirnadar.application.handlers
  (:require [kefirnadar.configuration.client :as client]
            [kefirnadar.application.queries :as q]
            [ring.util.http-response :as r]
            [taoensso.timbre :refer [infof]])
  (:import (java.util Date)))

(def conn (client/get-conn))

(defn get-users
  "This handler returns all the users."
  [input]
  (println (:path-params input))
  (let [users (q/ads (client/db) (:path-params input))]
    (if (not-empty users)
      (r/ok users)
      (r/bad-request {:status :error}))))

(defn create-user
  "Creates a user."
  [{:keys [parameters]}]
  (infof "Parameters: %s" parameters)
  (let [result (q/add-entity! conn
                              {:ad/firstname    (get-in parameters [:body :ad/firstname])
                               :ad/lastname     (get-in parameters [:body :ad/lastname])
                               :ad/region       (get-in parameters [:body :ad/region])
                               :ad/post         (get-in parameters [:body :ad/post] false)
                               :ad/pick-up      (get-in parameters [:body :ad/pick-up] false)
                               :ad/grains-kind  (get-in parameters [:body :ad/grains-kind])
                               :ad/quantity     (get-in parameters [:body :ad/quantity])
                               :ad/phone-number (get-in parameters [:body :ad/phone-number] "NOT PROVIDED")
                               :ad/email        (get-in parameters [:body :ad/email] "NOT PROVIDED")
                               :ad/created      (Date.)})
        new-user-id (-> result :tempids vals first)
        db-after (:db-after result)
        new-user (q/ad db-after new-user-id)]
    (if (:db-after result)
      (r/ok new-user)
      (r/bad-request))))

