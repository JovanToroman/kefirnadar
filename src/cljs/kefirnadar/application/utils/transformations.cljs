(ns kefirnadar.application.utils.transformations
  (:require [applied-science.js-interop :as j]
            [cuerdas.core :as cuerdas]
            [cuerdas.core :as str]))

(defn remove-reserved-characters [text]
  (cuerdas/replace text #"[\\^$.|?*+()\[\]{}]" ""))

(defn replace-serbian-characters
  [text]
  (str text (-> text
              (str/replace "č" "c")
              (str/replace "ć" "c")
              (str/replace "š" "s")
              (str/replace "ž" "z")
              (str/replace "đ" "dj")
              (str/replace "Č" "C")
              (str/replace "Ć" "C")
              (str/replace "Š" "S")
              (str/replace "Ž" "Z")
              (str/replace "Đ" "Dj"))))

(defn format-date
  ([date]
   (format-date date "sr-RS"))
  ([date locale]
   (j/call (js/Date. (js/Date.parse date)) :toLocaleDateString locale)))
