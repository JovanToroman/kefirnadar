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

(defn transaction-input [id]                                ;; ovo ne valja, namesticu da se pod transaction kljucem nalazi {:pick-up false :post false} kao default vrednosti
  (let [value (subscribe [::subs/form id])]                 ;; i da se pri cekiranju prvo pogleda vrednost pa onda rotira na true, i obrnuto
    [:div
     "Nacini transakcije:"
     [:div
      [:label "Slanje postom "]
      [:input {:value     "Slanje postom"
               :on-change #(dispatch [::events/update-form id (-> % .-target .-value)])
               :type      "checkbox"}]]
     [:div
      [:label "Licno preuzimanje"]
      [:input {:value     "Licno preuzimanje"
               :on-change #(dispatch [::events/update-form id (-> % .-target .-value)])
               :type      "checkbox"}]]]))

(defn qty-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label {:for "qty"} "Broj porcija koje delite? "]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (-> % .-target .-value)])
              :type        "number"
              :placeholder "Kolicina.."}]]))


(defn form []
  (let [is-valid? @(subscribe [::subs/is-valid? [:first-name :last-name :place :transaction :quantity]])]
    [:div
     [fname-input :first-name "Ime "]
     [lname-input :last-name]
     [place-input :place]
     [transaction-input :transaction]
     [qty-input :quantity]
     [:div
      [:button {:disabled (not is-valid?)                   ;; Nije moguce pritisnuti osim ako forma nije popunjena
                :on-click #(dispatch [::events/save-form])} "Posalji"]]]))

;; end form region




