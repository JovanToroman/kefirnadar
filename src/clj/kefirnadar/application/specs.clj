(ns kefirnadar.application.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::page-size (s/and pos-int? #{10 20 50}))
