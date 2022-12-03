(ns kefirnadar.application.views
  (:require [goog.string :as gstr]
            [kefirnadar.application.events :as events]
            [kefirnadar.application.regions :as r]
            [kefirnadar.application.subscriptions :as subs]
            [kefirnadar.application.validation :as validation]
            [kefirnadar.application.styles :as styles]
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
        valid? (subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Ime:"]
     [:input
      {:className   (css (:input-field styles/styles-map))
       :value       @value
       :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
       :on-blur     #(dispatch [::events/store-validation-results id (validation/field-validation id (extract-input-value %))])
       :type        "text"
       :required    true
       :placeholder "Vase ime..."}]
     (if (not @valid?) [:spam.text-danger {:className (css (:error styles/styles-map))} "Unesite vase ime."])]))


(defn last-name-input [id]
  (let [value (subscribe [::subs/form id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Prezime:"]
     [:input {:className   (css (:input-field styles/styles-map))
              :value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :on-blur     #(dispatch [::events/store-validation-results id (validation/field-validation id (extract-input-value %))])
              :type        "text"
              :placeholder "Vase prezime..."}]
     (if (not valid?) [:p.text-danger {:className (css (:error styles/styles-map))} "Unesite vase prezime."])]))


(defn region-select [id regions]
  (let [[css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Opstina:"]
     [:div {:className (css (:custom-select styles/styles-map))}
      [:input {:className   (css (:input-field styles/styles-map))
               :list "regions"
               :placeholder "Izabarite opstinu"
               :on-change #(dispatch [::events/update-form id (keyword (extract-input-value %))])}]
      [:datalist
       {:className (css (:select styles/styles-map))
        :id        "regions"}
       (map (fn [r] [:option {:key r :value r} r]) regions)]]]))


(defn email-input [id]
  (let [value (subscribe [::subs/form id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "E-mail:"]
     [:input {:className   (css (:input-field styles/styles-map))
              :value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :on-blur     #(dispatch [::events/store-validation-results id (validation/field-validation id (extract-input-value %))])
              :type        "text"
              :placeholder "xxxx@xxxx.xxx"}]
     (if (not valid?) [:p.text-danger {:className (css (:error styles/styles-map))} "Molimo vas proverite vasu email adresu i pokusajte ponovo."])]))


(defn phone-number-input [id]
  (let [value (subscribe [::subs/form id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Telefon:"]
     [:input {:className   (css (:input-field styles/styles-map))
              :value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :type        "text"
              :on-blur     #(dispatch [::events/store-validation-results id (validation/field-validation id (extract-input-value %))])
              :placeholder "06x-xxxx-xxxx"}]
     (if (not valid?) [:p.text-danger {:className (css (:error styles/styles-map))} "Molimo vas proverite vas broj telefona i pokusajte ponovo."] "")]))


(defn post-toggle [id]
  (let [value (subscribe [::subs/form id])
        [css] (styles/use-styletron)]
    [:div.form-group {:className (css (:input-wrapper styles/styles-map))}
     [:label {:className (css (:label styles/styles-map))} "Razmena postom?"]
     [:input {:className (css (:input-field styles/styles-map))
              :on-change #(dispatch [::events/update-form id (extract-checkbox-state %)])
              :type      "checkbox"
              :checked   @value}]]))


(defn pick-up-toggle [id]
  (let [value (subscribe [::subs/form id])
        [css] (styles/use-styletron)]
    [:div.form-group {:className (css (:input-wrapper styles/styles-map))}
     [:label {:className (css (:label styles/styles-map))} "Razmena uzivo?"]
     [:input {:className (css (:input-field styles/styles-map))
              :on-change #(dispatch [::events/update-form id (extract-checkbox-state %)])
              :type      "checkbox"
              :checked   @value}]]))

(defn qty-input [id]
  (let [value (subscribe [::subs/form id])
        valid? (subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Koju kolicinu delite?"]
     [:input {:className   (css (:input-field styles/styles-map))
              :value       @value
              :on-change   #(dispatch [::events/update-form id (long (extract-input-value %))])
              :on-blur     #(dispatch [::events/store-validation-results id (validation/field-validation id (extract-input-value %))])
              :type        "number"
              :min         "1"
              :max         "100"
              :placeholder "1-100"}]
     (if (not @valid?) [:p.text-danger {:className (css (:error styles/styles-map))} "Molimo proverite unetu kolicinu, vrednost mora biti izmedju 1 - 100."])]))



(defn form []
  (let [is-valid? @(subscribe [::subs/is-valid? [:firstname :lastname :region :quantity]])
        [css] (styles/use-styletron)]
    [:div {:className (css (:form-wrapper styles/styles-map))}
     [:div {:className (css (:wrapper-title styles/styles-map))} "Kreirajte vas oglas"]
     [:form
      [:div.form-group
       [first-name-input :firstname]
       [last-name-input :lastname]
       [region-select :region r/regions]
       [:div
        [:p {:className (css (:p styles/styles-map))} "Izaberite makar jedan nacini kontakta:"]
        [phone-number-input :phone-number]
        [email-input :email]]
       [:div
        [:p {:className (css (:p styles/styles-map))} "Izaberite makar jedan nacini transakcije:"]
        [post-toggle :post]
        [pick-up-toggle :pick-up]]
       [qty-input :quantity]]]
     [:div {:className (css (:input-field styles/styles-map))}
      [:button.btn.btn-outline-primary
       {:className (css (:btn styles/styles-map))
        :disabled  (not is-valid?)
        :on-click  #(dispatch [::events/create])} "Sacuvaj"]
      [:button.btn.btn-outline-primary
       {:className (css (:btn styles/styles-map))
        :on-click  #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Pocetna stranica"]]]))


;; Ovo cu promeniti, napravio sam ovako samo da bi video da li mi radi..
(defn user-row
  "A single user."
  [_users _reg-val]
  (for [user _users]
    [:tr
     [:td (:ad/firstname user)]
     [:td (:ad/lastname user)]
     [:td (:ad/region user)]
     [:td (if (:ad/phone-number user) (:ad/phone-number user) (gstr/unescapeEntities "&#10007"))]
     [:td (if (:ad/email user) (:ad/email user) (gstr/unescapeEntities "&#10007"))]
     [:td (if (:ad/post user) (gstr/unescapeEntities "&#10004") (gstr/unescapeEntities "&#10007"))]
     [:td (if (:ad/pick-up user) (gstr/unescapeEntities "&#10004") (gstr/unescapeEntities "&#10007"))]]))


(defn users-list
  "List of all users."
  []
  (let [region-value (subscribe [::subs/region])
        users (subscribe [::subs/users])
        [css] (styles/use-styletron)]
    [:div.d-flex.flex-column.min-vh-100.align-items-center
     [:button.btn.btn-outline-primary.col-md-5.mb-5 {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Pocetna stranica"]
     [:div
      [:label " Opstina: "]
      [:div
       [:input {:className   (css (:input-field styles/styles-map))
                :list "regions"
                :placeholder "Izabarite opstinu"
                :on-blur #(dispatch [::events/fetch-users (extract-input-value %)])}]
       [:datalist {:className (css (:select styles/styles-map))
                   :id        "regions"}
        [:option {:value ""} "Izaberite opštinu"]
        (map (fn [r] [:option {:key r :value r} r]) r/regions)]]]
     (when @users
       (if (= (count @users) 0)
         (dispatch [::events/fetch-users-fail]))
       [:div.table-responsive
        [:table.table.table-striped.table-bordered
         [:thead.thead-dark
          [:tr [:th {:scope "col"} "Ime"]
           [:th {:scope "col"} "Prezime"]
           [:th {:scope "col"} "Region"]
           [:th {:scope "col"} "Telefon"]
           [:th {:scope "col"} "E-mail"]
           [:th {:scope "col"} "Slanje postom"]
           [:th {:scope "col"} "Licno preuzimanje"]]]
         [:tbody
          (user-row @users @region-value)]]])]))


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

;; FORM



















