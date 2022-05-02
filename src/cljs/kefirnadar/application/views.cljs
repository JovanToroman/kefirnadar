(ns kefirnadar.application.views
  (:require [kefirnadar.application.events :as events]
            [kefirnadar.application.subscriptions :as subs]
            [re-frame.core :refer [dispatch subscribe]]))

(defn home []
  [:div [:h1 "Da li trazite ili delite kefir?"]
   [:button {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/delim}}])} "Delim"]
   [:button {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/trazim}}])} "Trazim"]])

;; -----
(defn delim []
  [:div [:h1 "Koju vrstu zrnaca delite?"]
   [:button {:on-click #(dispatch [::events/slucaj-delim (-> % .-target .-value)]) :value "MLEČNA"} "MLEČNA"]
   [:button {:on-click #(dispatch [::events/slucaj-delim (-> % .-target .-value)]) :value "VODENA"} "VODENA"]
   [:button {:on-click #(dispatch [::events/slucaj-delim (-> % .-target .-value)]) :value "KOMBUHA"} "KOMBUHA"]])

;; ----- ovo jos nije uradjeno, ignorisi
(defn trazim []
  [:div [:h1 "Koju vrstu zrnaca trazite?"]
   [:button {:on-click #(dispatch [::events/local-db-builder (-> % .-target .-value)]) :value "MLEČNA"} "MLEČNA"]
   [:button {:on-click #(dispatch [::events/local-db-builder (-> % .-target .-value)]) :value "VODENA"} "VODENA"]
   [:button {:on-click #(dispatch [::events/local-db-builder (-> % .-target .-value)]) :value "KOMBUHA"} "KOMBUHA"]])

;; form region
(defn fname-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Ime"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (-> % .-target .-value)])
              :type        "text"
              :placeholder "Vase ime..."}]]))

(defn lname-input [id]
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
     [:label {:for "place"} " Mesto "]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (-> % .-target .-value)])
              :type        "text"
              :placeholder "Mesto..."}]]))

(defn post-toggle [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Slanje postom"]
     [:input {:value     "Slanje postom"
              :on-change #(dispatch [::events/update-form id (-> % .-target .-checked)])
              :type      "checkbox"}]]))


(defn pick-up-toggle [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Licno preuzimanje"]
     [:input {:value     "Licno preuzimanje"
              :on-change #(dispatch [::events/update-form id (-> % .-target .-checked)])
              :type      "checkbox"
              :checked @value}]]))


(defn qty-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label {:for "qty"} "Broj porcija koje delite? "]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (-> % .-target .-value)])
              :type        "number"
              :placeholder "Kolicina.."}]]))


(defn form []
  (let [is-valid? @(subscribe [::subs/is-valid? [:first-name :last-name :place :quantity]])]
    [:div
     [fname-input :first-name "Ime "]
     [lname-input :last-name]
     [place-input :place]
     [:div
      "Nacini transakcije:"
      [post-toggle :post]
      [pick-up-toggle :pick-up]]
     [qty-input :quantity]
     [:div
      [:button {:disabled (not is-valid?)
                :on-click #(dispatch [::events/save-form])} "Posalji"]]]))

;; end form region




