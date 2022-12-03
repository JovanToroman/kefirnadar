(ns kefirnadar.application.utils
  (:require [cuerdas.core :as cuerdas]))

(defn remove-reserved-characters [text]
  (cuerdas/replace text #"[\\^$.|?*+()\[\]{}]" ""))