(ns kefirnadar.application.handlers
  (:require [kefirnadar.configuration.client :as client]))

(def conn (client/get-conn))
