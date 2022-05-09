(ns kefirnadar.application.views
  (:require [kefirnadar.application.events :as events]
            [kefirnadar.application.subscriptions :as subs]
            [re-frame.core :refer [dispatch subscribe]]))

(defn home []
  [:div [:h1 "Da li delite ili trazite kefir?"]
   [:button {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/delim}}])} "Delim"]
   [:button {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/trazim}}])} "Trazim"]])

;; -----
(defn selling []
  [:div [:h1 "Koji tip zrnaca delite?"]
   [:button {:on-click #(dispatch [::events/selling (-> % .-target .-value)]) :value :milk-type} "Mlecni"]
   [:button {:on-click #(dispatch [::events/selling (-> % .-target .-value)]) :value :water-type} "Vodeni"]
   [:button {:on-click #(dispatch [::events/selling (-> % .-target .-value)]) :value :kombucha} "Kombuha"]])

;; ----- ovo jos nije uradjeno, ignorisi
(defn purchasing []
  [:div [:h1 "Koji tip zrnaca trazite?"]
   [:button {:on-click #(dispatch [::events/local-db-builder (-> % .-target .-value)]) :value :milk-type} "Mlecni"]
   [:button {:on-click #(dispatch [::events/local-db-builder (-> % .-target .-value)]) :value :water-type} "Vodeni"]
   [:button {:on-click #(dispatch [::events/local-db-builder (-> % .-target .-value)]) :value :kombucha} "Kombuha"]])

;; form region
(defn first-name-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Ime"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (-> % .-target .-value)])
              :type        "text"
              :placeholder "Vase ime..."}]]))

(defn last-name-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Prezime"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (-> % .-target .-value)])
              :type        "text"
              :placeholder "Vase prezime..."}]]))

(defn place-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label " Mesto "]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (-> % .-target .-value)])
              :type        "text"
              :placeholder "Mesto na kom delite..."}]]))

(defn post-toggle [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Razmena postom?"]
     [:input {:on-change #(dispatch [::events/update-form id (-> % .-target .-checked)])
              :type      "checkbox"
              :checked   @value}]]))


(defn pick-up-toggle [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Razmena uzivo?"]
     [:input {:on-change #(dispatch [::events/update-form id (-> % .-target .-checked)])
              :type      "checkbox"
              :checked   @value}]]))


(defn qty-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label {:for "qty"} "Koju kolicinu delite?"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (-> % .-target .-value)])
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




