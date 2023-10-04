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

(def smtp-host (delay (get-in @config [:imejl :host])))
(def smtp-korisnik (delay (get-in @config [:imejl :korisnik])))
(def smtp-lozinka (delay (get-in @config [:imejl :lozinka])))
(def smtp-port (delay (get-in @config [:imejl :port])))

(def nrepl-port (delay (get-in @config [:nrepl :port])))
