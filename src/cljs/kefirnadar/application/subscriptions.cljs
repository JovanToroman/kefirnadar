(ns kefirnadar.application.subscriptions
  (:require [re-frame.core :refer [trim-v reg-event-db reg-event-fx reg-sub]]
            [kefirnadar.application.regions :as r]))




(reg-sub
  ::filtered-regions-coll
  (fn [db]
    (let [regions-filter-param (get-in db [:user :data :region-filter])
          keywords-to-strings-coll (map #(name %) r/regions)
          filtered-regions (filter #(clojure.string/includes? % regions-filter-param) keywords-to-strings-coll)] ;; TODO: Da li mozda zelimo da nam po defaultu r/regions bude lista sa stringovima??
      (cond
        (seq? filtered-regions) (map #(keyword %) filtered-regions)
        :else r/regions))))

(reg-sub
  ::form
  (fn [db [_ id]]
    (get-in db [:form id] "")))

(reg-sub
  ::form-validation
  (fn [db [_ id]]
    (get-in db [:form-validation id] true)))


;; helper function for ::is-valid?

(defn contains-any? [map & keys]
  (some #(contains? map %) keys))


(reg-sub
  ::is-valid?
  (fn [db [_ form-ids]]
    (and (every? #(get-in db [:form %]) form-ids)
         (every? #(true? (val %)) (:form-validation db))
         (contains-any? (:form db) :post :pick-up)
         (contains-any? (:form db) :phone-number :email))))


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


