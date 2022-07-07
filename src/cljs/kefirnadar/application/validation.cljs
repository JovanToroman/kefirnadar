(ns kefirnadar.application.validation
  (:require [cuerdas.core :as str]))


(def email-regex-str
  "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")


(def phone-number-regex-str
  "0[0-9]{2}[/-][0-9]{3,4}- ?[0-9]{3,4}")


(defn reg-matcher
  [regex-str input]
  (let [matcher (partial re-matches (re-pattern regex-str))]
    (some? ((fnil matcher "") input))))


(defn field-validation [id input]
  (case id
    :firstname (and (not (str/blank? input)) (> (count input) 1) (str/alpha? input))
    :lastname (and (not (str/blank? input)) (> (count input) 1) (str/alpha? input))
    :email (or (str/empty? input) (reg-matcher email-regex-str input))
    :phone-number (or (str/empty? input) (reg-matcher phone-number-regex-str input))
    :quantity (and (< input 101) (> input 0))
    ))
