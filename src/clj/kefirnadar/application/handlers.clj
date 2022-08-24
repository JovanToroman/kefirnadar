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
  (let [users (q/users (client/db) (:path-params input))]
    (if (not-empty users)
      (r/ok users)
      (r/bad-request {:status :error}))))

(defn create-user
  "Creates a user."
  [{:keys [parameters]}]
  (infof "Parameters: %s" parameters)
  (let [result (q/add-entity! conn
                              {:user/firstname    (get-in parameters [:body :user/firstname])
                               :user/lastname     (get-in parameters [:body :user/lastname])
                               :user/region       (get-in parameters [:body :user/region])
                               :user/post         (get-in parameters [:body :user/post] false)
                               :user/pick-up      (get-in parameters [:body :user/pick-up] false)
                               :user/grains-kind  (get-in parameters [:body :user/grains-kind])
                               :user/quantity     (get-in parameters [:body :user/quantity])
                               :user/phone-number (get-in parameters [:body :user/phone-number] "NOT PROVIDED")
                               :user/email        (get-in parameters [:body :user/email] "NOT PROVIDED")
                               :user/created      (new Date)})
        new-user-id (-> result :tempids vals first)
        db-after (:db-after result)
        new-user (q/user db-after new-user-id)]
    (if (:db-after result)
      (r/ok new-user)
      (r/bad-request))))

