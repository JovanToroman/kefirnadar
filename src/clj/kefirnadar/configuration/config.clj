(ns kefirnadar.configuration.config
  (:require [clojure.java.io :as io]
            [mount.core :as mount]
            [aero.core :as aero]))

(defn read-config [{:keys [profile]
                    :or   {profile :dev}}]
  (-> (io/resource "config.edn")
    (aero/read-config {:profile profile})))

(def config (read-config (mount/args)))