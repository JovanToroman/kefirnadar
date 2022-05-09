(ns kefirnadar.application.views
  (:require [kefirnadar.application.events :as events]
            [kefirnadar.application.subscriptions :as subs]
            [re-frame.core :refer [dispatch subscribe]]))

;; -- helper functions region --
(defn extract-input-value
  [event]
  (-> event .-target .-value))

(defn extract-checkbox-state
  [event]
  (-> event .-target .-checked))
;; -- end helper functions region --


;; form region ( case :ad-type :sharing )
(defn first-name-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Ime"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :type        "text"
              :placeholder "Vase ime..."}]]))

(defn last-name-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Prezime"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :type        "text"
              :placeholder "Vase prezime..."}]]))

(defn place-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label " Mesto "]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :type        "text"
              :placeholder "Mesto na kom delite..."}]]))

(defn post-toggle [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Razmena postom?"]
     [:input {:on-change #(dispatch [::events/update-form id (extract-checkbox-state %)])
              :type      "checkbox"
              :checked   @value}]]))


(defn pick-up-toggle [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Razmena uzivo?"]
     [:input {:on-change #(dispatch [::events/update-form id (extract-checkbox-state %)])
              :type      "checkbox"
              :checked   @value}]]))


(defn qty-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label {:for "qty"} "Koju kolicinu delite?"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :type        "number"
              :placeholder "Kolicina koju delite..."}]]))

(defn form []
  (let [is-valid? @(subscribe [::subs/is-valid? [:first-name :last-name :place :quantity]])]
    [:div
     [first-name-input :first-name]
     [last-name-input :last-name]
     [place-input :place]
     [:div
      "Nacini transakcije:"
      [post-toggle :post]
      [pick-up-toggle :pick-up]]
     [qty-input :quantity]
     [:div
      [:button {:disabled (not is-valid?)
                :on-click #(dispatch [::events/save-form])} "Sacuvaj"]]]))

;; end form region

;; case :ad-type :looking-for region

(defn NOT-DONE-YET []
  [:h1 "RANDOM PAGE"])

;; end case :ad-type :looking-for region

(defn home []
  [:div [:h1 "Da li delite ili trazite kefir?"]
   [:button {:on-click #(dispatch [::events/ad-type {:type :sharing}])} "Delim"]
   [:button {:on-click #(dispatch [::events/ad-type {:type :looking-for}])} "Trazim"]])

(defn grains-kind []
  [:div
   [:button {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :milk-type} "Mlecni"]
   [:button {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :water-type} "Vodeni"]
   [:button {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :kombucha} "Kombuha"]])


(defn choice []
  (let [choice @(subscribe [::subs/choice])]
    (case choice
      :sharing [form]
      :looking-for [NOT-DONE-YET])))








