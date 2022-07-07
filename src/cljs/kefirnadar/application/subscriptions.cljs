(ns kefirnadar.application.subscriptions
  (:require [re-frame.core :refer [trim-v reg-event-db reg-event-fx reg-sub]]
            [cuerdas.core :as str]))

;; sub final validation

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
    :lastname  (and (not (str/blank? input)) (> (count input) 1) (str/alpha? input))
    :email    (or (str/blank? input) (reg-matcher email-regex-str input))
    :phone-number (or (str/blank? input) (reg-matcher phone-number-regex-str input))
    :quantity (and (< input 101) (> input 0))
    ))

(reg-sub
  ::form
  (fn [db [_ id]]
    (get-in db [:form id] "")))


(reg-sub
  ::is-valid?
  (fn [db [_ form-ids]]
    (and (every? #(get-in db [:form %]) form-ids)
         (every? #(true? (val %)) (db :form-validation)))))

#_(reg-sub
  ::is-value-valid?
  (fn [db _]
    (every? #(true? (val %)) (db :form-validation))
    #_(every? #(get-in db (vals %)) [:form-validation db])))


(reg-sub
  ::ad-type-choice
  (fn [db _]
    (keyword (get-in db [:active-route :parameters :path :ad-type]))))


(reg-sub
  ::users
  (fn [db _] (get db :all-users)))


(reg-sub
  ::region
  (fn [db _] (get-in db [:user :data :region-filter] "")))


