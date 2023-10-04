(ns kefirnadar.application.async
  (:require [kefirnadar.application.db :as q]
            [taoensso.timbre :as log]))


(def ^Long day-in-milliseconds (* 1000 60 60 24))

(defn remove-old-ads-thread [running?]
  "Check for ads older then predefined time, retrieve ids and feed them to a tx-retract-old-ads function."
  (Thread.
    ^Runnable
    (while running?
      (try
        (let [old-ad-ids (map :ad/ad_id (q/get-expired-ads))]
          (if (empty? old-ad-ids)
            (log/info "No ads older than 30 days to retract.")
            (q/remove-old-ads old-ad-ids)))
        (catch Exception e
          (log/info "Exception in retract-old-ads-thread:" e)))
      (Thread/sleep day-in-milliseconds))))