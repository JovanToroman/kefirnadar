(ns kefirnadar.application.validation
  (:require [cuerdas.core :as str]))


(def email-regex-str
  "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")


(def broj-telefona-regex-str
  "[06|+][0-9]{1,5}[/-\\s]*[0-9]{1,4}[/-\\s]*[0-9]{2,4}[/-\\s]*[0-9]{2,4}[\\s]*[0-9]{2,4}[\\s]*")

(def password-regex
  "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$")

(defn reg-matcher
  [regex-str input]
  (let [matcher (partial re-matches (re-pattern regex-str))]
    (some? ((fnil matcher "") input))))


(defn potvrdi-vrednost-polja [kljuc-polja vrednost-polja]
  (case kljuc-polja
    :firstname (and (not (str/blank? vrednost-polja)) (seq vrednost-polja) (str/letters? vrednost-polja))
    :lastname (and (not (str/blank? vrednost-polja)) (seq vrednost-polja) (str/letters? vrednost-polja))
    ;; TODO: unify these two by using imejl only
    :imejl (reg-matcher email-regex-str vrednost-polja)
    (:slanje? :preuzimanje?) (true? vrednost-polja)
    (:deli-mlecni? :deli-vodeni? :deli-kombucu?) (true? vrednost-polja)
    :broj-telefona (reg-matcher broj-telefona-regex-str vrednost-polja)
    :lozinka (reg-matcher password-regex vrednost-polja)
    :nova-lozinka (reg-matcher password-regex vrednost-polja)
    :korisnicko-ime (and (string? vrednost-polja) (not (str/blank? vrednost-polja)) (> (count vrednost-polja) 4))
    :poruka (and (string? vrednost-polja) (not (str/blank? vrednost-polja)) (> (count vrednost-polja) 19))

    :oblast (and (string? vrednost-polja) (not (str/blank? vrednost-polja)))

    :kontakt (or (reg-matcher email-regex-str (:imejl vrednost-polja))
               (reg-matcher broj-telefona-regex-str (:broj-telefona vrednost-polja)))

    :nacin-deljenja (or (true? (:slanje? vrednost-polja)) (true? (:preuzimanje? vrednost-polja)))

    :vrsta-kulture (or (true? (:deli-mlecni? vrednost-polja)) (true? (:deli-vodeni? vrednost-polja))
                     (true? (:deli-kombucu? vrednost-polja)))))

(defn either-or-form-field-valid?
  "Check whether at least one option was selected"
  [validation-info & form-ids]
  (some (fn [form-id]
          (true? (get validation-info form-id)))
    form-ids))

(defn sharing-form-valid?
  "Is the whole sharing form valid?"
  [validation-info]
  (every? true? (vals validation-info)))

(defn either-or-update-validation
  "Some fields require at least one option to be selected. Mark all related fields as valid if at least one of them is"
  [validation-info]
  (cond-> validation-info
    (either-or-form-field-valid? validation-info :slanje? :preuzimanje?) (assoc :slanje? true :preuzimanje? true)
    (either-or-form-field-valid? validation-info :imejl :broj-telefona) (assoc :imejl true :broj-telefona true)
    (either-or-form-field-valid? validation-info :deli-mlecni? :deli-vodeni? :deli-kombucu?)
    (assoc :deli-mlecni? true :deli-vodeni? true :deli-kombucu? true)))

(defn validate-form-info
  "Validates form field info and stores results in app db, upon which error messages are then displayed"
  [form-info]
  (->> form-info
    (map (fn [[form-field-key form-field-value]]
           [form-field-key (potvrdi-vrednost-polja form-field-key form-field-value)]))
    (into {})))

(defn forma-validna?
  [validation-info]
  (every? (comp true? second) validation-info))
