(ns kefirnadar.application.views
  (:require [kefirnadar.application.events :as events]
            [kefirnadar.application.subscriptions :as subs]
            [re-frame.core :refer [dispatch subscribe]]))

(defn home []
  [:div [:h1 "Are you purchasing or selling kefir?"]
   [:button {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/delim}}])} "Selling"]
   [:button {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/trazim}}])} "Purchasing"]])

;; -----
(defn selling []
  [:div [:h1 "Which type are u selling?"]
   [:button {:on-click #(dispatch [::events/selling (-> % .-target .-value)]) :value :milk-type} "Milk type"]
   [:button {:on-click #(dispatch [::events/selling (-> % .-target .-value)]) :value :water-type} "Water type"]
   [:button {:on-click #(dispatch [::events/selling (-> % .-target .-value)]) :value :kombucha} "Kombucha"]])

;; ----- ovo jos nije uradjeno, ignorisi
(defn purchasing []
  [:div [:h1 "Which type are u purchasing?"]
   [:button {:on-click #(dispatch [::events/local-db-builder (-> % .-target .-value)]) :value :milk-type} "Milk type"]
   [:button {:on-click #(dispatch [::events/local-db-builder (-> % .-target .-value)]) :value :water-type} "Water type"]
   [:button {:on-click #(dispatch [::events/local-db-builder (-> % .-target .-value)]) :value :kombucha} "Kombucha"]])

;; form region
(defn fname-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "First name"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (-> % .-target .-value)])
              :type        "text"
              :placeholder "Your first name...."}]]))

(defn lname-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Last name"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (-> % .-target .-value)])
              :type        "text"
              :placeholder "Your last name..."}]]))

(defn place-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label " Place "]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (-> % .-target .-value)])
              :type        "text"
              :placeholder "Place... "}]]))

(defn post-toggle [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Post Express"]
     [:input {:on-change #(dispatch [::events/update-form id (-> % .-target .-checked)])
              :type      "checkbox"
              :checked   @value}]]))


(defn pick-up-toggle [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Live exchange"]
     [:input {:on-change #(dispatch [::events/update-form id (-> % .-target .-checked)])
              :type      "checkbox"
              :checked   @value}]]))


(defn qty-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label {:for "qty"} "Quantity for distributing?"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (-> % .-target .-value)])
              :type        "number"
              :placeholder "Quantity.."}]]))


(defn form []
  (let [is-valid? @(subscribe [::subs/is-valid? [:first-name :last-name :place :quantity]])]
    [:div
     [fname-input :first-name]
     [lname-input :last-name]
     [place-input :place]
     [:div
      "Ways of transaction"
      [post-toggle :post]
      [pick-up-toggle :pick-up]]
     [qty-input :quantity]
     [:div
      [:button {:disabled (not is-valid?)
                :on-click #(dispatch [::events/save-form])} "Save form"]]]))

;; end form region




