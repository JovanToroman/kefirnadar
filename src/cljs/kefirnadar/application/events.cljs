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
  ::selling
  (fn [cofx [_ val]]
    {::set-item! [:seed-type val]
     ::load-route! {:data {:name :route/form}}}))


(reg-event-fx
  ::slucaj-trazim
  (fn [cofx [_ val]]
    {:db (assoc (:db cofx) :vrsta-zrnca val)
     ::load-route! {:data {:name :route/form}}}))


;; form events region

(reg-event-db
  ::update-form
  (fn [db [_ id val]]
    (assoc-in db [:form id] val)))

;; *** sta ako korisnik zeli da napravi prodaju za vise vrsta zrnaca? imam resenje za ovo.. razmotriti.
(reg-event-db                                               ;; Kasnije zelim da ovo prebacim u -fx i mozda da se preko ovoga pokrece cuvanje u bazi :user da prosledimo kao tx-data???
  ::save-form
  (fn [db]
    (let [type (localstorage/get-item :seed-type)
          form-data (assoc (:form db) :type type)]
      (-> db
          (assoc :user form-data)
          (dissoc :form)))))

;; end form events region