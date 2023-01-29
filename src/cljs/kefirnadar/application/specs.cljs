(ns kefirnadar.application.specs
  (:require [cljs.spec.alpha :as s]))

(s/def ::user-action #{:sharing :seeking})
