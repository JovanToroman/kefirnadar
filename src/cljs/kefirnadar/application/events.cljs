(ns kefirnadar.application.events
  (:require [kefirnadar.configuration.routes :as routes]
            [kefirnadar.application.localstorage :as localstorage]
            [re-frame.core :refer [reg-event-fx reg-fx trim-v reg-event-db dispatch]]))

;; -route events-
(reg-fx ::load-route! routes/load-route!)
;; -end route events-

;; -localstorage events-
(reg-fx ::set-item! localstorage/set-item!)
(reg-fx ::get-item localstorage/get-item)
(reg-fx ::remove-item localstorage/remove-item!)
;; -end localstorage events-


(defn dispatch-load-route!
  "A coeffect to dispatch the load-route! effect."
  [_ [route]]
  {::load-route! route})

(reg-event-fx ::dispatch-load-route! [trim-v] dispatch-load-route!)

(reg-event-fx
  ::ad-type
  (fn [{db :db} [_ {type :type}]]
    {:db (assoc-in db [:user :data :ad-type] type)
     ::set-item!   [:ad-type type]
     ::load-route! {:data {:name :route/grains-kind}}}))

(reg-event-fx
  ::grains-kind
  (fn [{db :db} [_ type]]
    {:db (assoc-in db [:user :data :grains-kind] (keyword type))
     ::set-item!   [:grains-kind (keyword type)]
     ::load-route! {:data {:name :route/choice}}}))

;; form events region

(reg-event-db
  ::update-form
  (fn [db [_ id val]]
    (assoc-in db [:form id] val)))

;; *** sta ako korisnik zeli da napravi prodaju za vise vrsta zrnaca? imam resenje za ovo.. razmotriti.
(reg-event-db                                               ;; Kasnije zelim da ovo prebacim u -fx i mozda da se preko ovoga pokrece cuvanje u bazi :user da prosledimo kao tx-data???
  ::save-form
  (fn [db]
    (let [type (localstorage/get-item :grains-kind)
          form-data (assoc (:form db) :grains-kind (symbol type))]
      (-> db
          (assoc-in [:user :form] form-data)
          (dissoc :form)))))

;; end form events region