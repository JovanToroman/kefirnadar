(ns kefirnadar.application.validation
  (:require [cuerdas.core :as str]))


(def email-regex-str
  "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")


(def phone-number-regex-str
  "[06|+][0-9]{1,5}[/-\\s]*[0-9]{1,4}[/-\\s]*[0-9]{2,4}[/-\\s]*[0-9]{2,4}[\\s]*[0-9]{2,4}[\\s]*")


(defn reg-matcher
  [regex-str input]
  (let [matcher (partial re-matches (re-pattern regex-str))]
    (some? ((fnil matcher "") input))))


(defn field-validation [id input]
  (case id
    :firstname (and (not (str/blank? input)) (seq input) (str/alpha? input))
    :lastname (and (not (str/blank? input)) (seq input) (str/alpha? input))
    :email (reg-matcher email-regex-str input)
    :region (keyword? input)
    (:post :pick-up) (true? input)
    :phone-number (reg-matcher phone-number-regex-str input)
    :quantity (and (< input 101) (> input 0))))

(defn either-or-form-field-valid?
  "Check whether at least one option was selected"
  [validation-info & form-ids]
  (some (fn [form-id]
          (true? (get validation-info form-id)))
    form-ids))

(defn form-valid?
  "Is the whole form valid?"
  [validation-info & form-ids]
  (and (every? #(get validation-info %) form-ids)
    (either-or-form-field-valid? validation-info :post :pick-up)
    (either-or-form-field-valid? validation-info :phone-number :email)))

(defn- either-or-update-validation
  "Some fields require at least one option to be selected. Mark all related fields as valid if at least one of them is"
  [validation-info]
  (cond-> validation-info
    (either-or-form-field-valid? validation-info :post :pick-up) (assoc :post true :pick-up true)
    (either-or-form-field-valid? validation-info :email :phone-number) (assoc :email true :phone-number true)))

(defn validate-form-info
  "Validates form field info and stores results in app db, upon which error messages are then displayed"
  [form-info]
  (->> form-info
    (map (fn [[form-field-key form-field-value]]
           [form-field-key (field-validation form-field-key form-field-value)]))
    (into {})
    either-or-update-validation))
