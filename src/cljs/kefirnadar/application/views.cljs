(ns kefirnadar.application.views
  (:require [goog.string :as gstr]
            [kefirnadar.application.events :as events]
            [kefirnadar.application.regions :as r]
            [kefirnadar.application.subscriptions :as subs]
            [kefirnadar.application.validation :as validation]
            [re-frame.core :refer [dispatch subscribe]]))




;; -- helper functions region --
(defn extract-input-value
  [event]
  (-> event .-target .-value))

(defn extract-checkbox-state
  [event]
  (-> event .-target .-checked))


;; -- end helper functions region --


(defn first-name-input [id]
  (let [value (subscribe [::subs/form id])
        valid? (subscribe [::subs/form-validation id])]
    [:div.d-flex.flex-column.justify-content-center.align-items-center.mb-3
     [:label "Ime: "]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :on-blur     #(dispatch [::events/validate-form id (validation/field-validation id (extract-input-value %))])
              :type        "text"
              :required    true
              :placeholder "Vase ime..."}]
     (if (not @valid?) [:p.text-danger "  Unesite vase ime."])]))


(defn last-name-input [id]
  (let [value (subscribe [::subs/form id])
        valid? @(subscribe [::subs/form-validation id])]
    [:div.d-flex.flex-column.justify-content-center.align-items-center.mb-3
     [:label "Prezime: "]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :on-blur     #(dispatch [::events/validate-form id (validation/field-validation id (extract-input-value %))])
              :type        "text"
              :placeholder "Vase prezime..."}]
     (if (not valid?) [:p.text-danger "  Unesite vase prezime."])]))


(defn region-select [id regions]
  (let [value (subscribe [::subs/form id])]
    [:div.d-flex.flex-column.justify-content-center.align-items-center.mb-3
     [:label "Opstina:"]
     [:select {:value     @value
               :on-change #(dispatch [::events/update-form id (keyword (extract-input-value %))])}
      [:option {:value ""} "Izabarite opstinu"]
      (map (fn [r] [:option {:key r :value r} r]) regions)]]))


(defn email-input [id]
  (let [value (subscribe [::subs/form id])
        valid? @(subscribe [::subs/form-validation id])]
    [:div.d-flex.flex-column.justify-content-center.align-items-center.mb-3
     [:label "E-mail: "]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :on-blur     #(dispatch [::events/validate-form id (validation/field-validation id (extract-input-value %))])
              :type        "text"
              :placeholder "xxxx@xxxx.com"}]
     (if (not valid?) [:p.text-danger "  Molimo vas proverite vasu email adresu i pokusajte ponovo."])]))

(defn phone-number-input [id]
  (let [value (subscribe [::subs/form id])
        valid? @(subscribe [::subs/form-validation id])]
    [:div.d-flex.flex-column.justify-content-center.align-items-center.mt-3
     [:label "Telefon:"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :type        "text"
              :on-blur     #(dispatch [::events/validate-form id (validation/field-validation id (extract-input-value %))])
              :placeholder "06x-xxxx-xxxx"}]
     (if (not valid?) [:p.text-danger "  Molimo vas proverite vas broj telefona i pokusajte ponovo."] "")]))

(defn post-toggle [id]
  (let [value (subscribe [::subs/form id])]
    [:div.mt-3
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
  (let [value (subscribe [::subs/form id])
        valid? (subscribe [::subs/form-validation id])]
    [:div:div.d-flex.flex-column.justify-content-center.align-items-center.mt-3.mb-2
     [:label {:for "qty"} "Koju kolicinu delite?"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (long (extract-input-value %))])
              :on-blur     #(dispatch [::events/validate-form id (validation/field-validation id (extract-input-value %))])
              :type        "number"
              :min         "1"
              :max         "100"
              :placeholder "1-100"}]
     (if (not @valid?) [:p.text-danger "  Molimo proverite unetu kolicinu, vrednost mora biti izmedju 1 - 100."])]))


(defn form []
  (let [is-valid? @(subscribe [::subs/is-valid? [:firstname :lastname :region :quantity]])]
    [:div.d-flex.flex-column.min-vh-100.justify-content-center.align-items-center.border
     [:div.border-left.border-right.pl-3.pr-3
      [first-name-input :firstname]
      [last-name-input :lastname]
      [region-select :region r/regions]
      [:div.d-flex.flex-column.justify-content-center.align-items-center.mb-3
       "Izaberite makar jedan nacini kontakta:"
       [phone-number-input :phone-number]
       [email-input :email]]
      [:div.d-flex.flex-column.justify-content-center.align-items-center.mb-3
       "Izaberite makar jedan nacini transakcije:"
       [post-toggle :post]
       [pick-up-toggle :pick-up]]
      [qty-input :quantity]]
     [:div.border-bottom
      [:button.btn.btn-outline-primary {:disabled (not is-valid?)
                                        :on-click #(dispatch [::events/create])} "Sacuvaj"]]]))


;; Ovo cu promeniti, napravio sam ovako samo da bi video da li mi radi..
(defn user-row
  "A single user."
  [_users _reg-val]
  (for [user _users]
    [:tbody
     [:tr {:align "left"
           :style {:border "1px solid black"
                   :width  "100%"}}
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (:user/firstname user)]
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (:user/lastname user)]
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (:user/region user)]
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (:user/phone-number user)]
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (:user/email user)]
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (if (:user/post user) (gstr/unescapeEntities "&#10004") (gstr/unescapeEntities "&#10007"))]
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (if (:user/pick-up user) (gstr/unescapeEntities "&#10004") (gstr/unescapeEntities "&#10007"))]]]))


(defn users-list
  "List of all users."
  []
  (let [region-value (subscribe [::subs/region])
        users (subscribe [::subs/users])]
    [:div:div.d-flex.flex-column.min-vh-100.align-items-center
     [:button.btn.btn-outline-primary.col-md-5.mb-5 {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Pocetna stranica"]
     [:div
      [:label " Opstina: "]
      [:div
       [:select {:value     @region-value
                 :on-change #(dispatch [::events/fetch-users (keyword (extract-input-value %))])}
        [:option {:value ""} "Izabarite opstinu"]
        (map (fn [r] [:option {:key r :value r} r]) r/regions)]]]
     (when @users
       [:table.mt-5 {:style {:border          "1px solid black"
                             :border-collapse "collapse"
                             :width           "100%"}}
        [:tbody
         [:tr {:style {:width "100%"}}
          [:th {:style {:border "1px solid black"}} "Ime"]
          [:th {:style {:border "1px solid black"}} "Prezime"]
          [:th {:style {:border "1px solid black"}} "Region"]
          [:th {:style {:border "1px solid black"}} "Telefon"]
          [:th {:style {:border "1px solid black"}} "E-mail"]
          [:th {:style {:border "1px solid black"}} "Slanje postom"]
          [:th {:style {:border "1px solid black"}} "Licno preuzimanje"]]]
        (user-row @users @region-value)])]))


(defn home []
  [:div.d-flex.flex-column.min-vh-100.justify-content-center.align-items-center
   [:h1 "Da li delite ili trazite kefir?"]
   [:button.btn.btn-outline-primary.col-md-5.mb-5.mt-5 {:on-click #(dispatch [::events/ad-type {:type :sharing}])} "Delim"]
   [:button.btn.btn-outline-primary.col-md-5 {:on-click #(dispatch [::events/ad-type {:type :seeking}])} "Trazim"]])


(defn grains-kind []
  [:div.d-flex.flex-column.min-vh-100.justify-content-center.align-items-center
   [:button.btn.btn-outline-primary.col-md-5.mb-5.mt-5 {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :milk-type} "Mlecni"]
   [:button.btn.btn-outline-primary.col-md-5.mb-5 {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :water-type} "Vodeni"]
   [:button.btn.btn-outline-primary.col-md-5 {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :kombucha} "Kombuha"]])


(defn ad-type-choice []
  (case @(subscribe [::subs/ad-type-choice])
    :sharing form
    :seeking users-list))

(defn thank-you []
  [:div.d-flex.flex-column.min-vh-100.justify-content-center.align-items-center [:h1.mb-5 "Hvala vam sto delite kefir zrnca"]
   [:button.btn.btn-outline-success.mb-5 {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Pocetna stranica"]])

(defn error []
  [:div.d-flex.flex-column.min-vh-100.justify-content-center.align-items-center
   [:h1 "Trenutno nema korisnika koji dele zrnca u izabranom regionu."]
   [:button.btn.btn-outline-warning {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Pocetna stranica"]])

