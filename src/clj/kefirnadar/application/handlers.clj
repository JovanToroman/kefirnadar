(ns kefirnadar.application.handlers
  (:require [kefirnadar.configuration.client :as client]
            [kefirnadar.application.queries :as q]
            [ring.util.http-response :as r]))

(def conn (client/get-conn))

(defn get-users
  "This handler returns all the users."
  [{:keys [_params]}]
  (let [users (q/users (client/db))]
    (if (not-empty users)
      (r/ok users)
      (r/bad-request {:status :error}))))
