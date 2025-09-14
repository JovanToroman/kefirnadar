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

(def adresa-posiljaoca (delay (get-in @config [:imejl :adresa-posiljaoca])))
(def api-kljuc (delay (get-in @config [:imejl :api-kljuc])))
(def api-tajna (delay (get-in @config [:imejl :api-tajna])))

(def nrepl-port (delay (get-in @config [:nrepl :port])))
