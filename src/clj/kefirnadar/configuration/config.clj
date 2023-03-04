(ns kefirnadar.configuration.config
  (:require [clojure.java.io :as io]
            [mount.core :as mount]
            [aero.core :as aero]
            [taoensso.timbre :as log]))

(defn read-config [{:keys [profile]
                    :or   {profile :dev}}]
  (log/debug "read-config profile: " profile)
  (-> (io/resource "config.edn")
    (aero/read-config {:profile profile})))

(def config (delay (read-config (mount/args))))

(def postgres-main (delay (get-in @config [:postgres :main])))
