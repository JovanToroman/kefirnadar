(ns kefirnadar.common.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::regions (or string? (s/coll-of string?)))
