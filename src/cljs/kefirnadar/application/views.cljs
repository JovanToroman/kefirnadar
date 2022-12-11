(ns kefirnadar.application.views
  (:require [goog.string :as gstr]
            [kefirnadar.application.events :as events]
            [kefirnadar.application.subscriptions :as subs]
            [kefirnadar.application.validation :as validation]
            [kefirnadar.application.styles :as styles]
            [kefirnadar.application.inputs :as inputs]
            [re-frame.core :refer [dispatch subscribe]]
            [kefirnadar.application.regions :as r]
            [applied-science.js-interop :as j]
            [kefirnadar.application.utils :as utils]))


;; -- helper functions region --
(defn extract-input-value
  [event]
  (j/get-in event [:target :value]))

(defn extract-checkbox-state
  [event]
  (j/get-in event [:target :checked]))


;; -- end helper functions region --

(defn first-name-input [id]
  (let [value (subscribe [::subs/form-field id])
        valid? (subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Ime:"]
     [:input
      {:className   (css (:input-field styles/styles-map))
       :value       @value
       :on-change   #(dispatch [::events/update-sharing-form id (extract-input-value %)])
       :on-blur     #(dispatch [::events/store-sharing-form-validation-results id (validation/field-validation id (extract-input-value %))])
       :type        "text"
       :required    true
       :placeholder "Vase ime..."}]
     (if (not @valid?) [:spam.text-danger {:className (css (:error styles/styles-map))} "Unesite vase ime."])]))


(defn last-name-input [id]
  (let [value (subscribe [::subs/form-field id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Prezime:"]
     [:input {:className   (css (:input-field styles/styles-map))
              :value       @value
              :on-change   #(dispatch [::events/update-sharing-form id (extract-input-value %)])
              :on-blur     #(dispatch [::events/store-sharing-form-validation-results id (validation/field-validation id (extract-input-value %))])
              :type        "text"
              :placeholder "Vase prezime..."}]
     (if (not valid?) [:p.text-danger {:className (css (:error styles/styles-map))} "Unesite vase prezime."])]))


(defn region-select [id]
  (let [selected-region (subscribe [::subs/form-field id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Opština:"]
     [:div {:className (css (:custom-select styles/styles-map))}
      [inputs/search-selector {:placeholder "Izaberite mesto"
                               :options (map (fn [r] {:title (name r)
                                                      :value r
                                                      :title-cleaned (utils/replace-serbian-characters (name r))
                                                      :on-click (fn [event]
                                                                  (dispatch [::events/update-sharing-form id
                                                                             (keyword (extract-input-value event))]))})
                                          r/regions)
                               :active-value @selected-region
                               :aria-labelledby "learning-spaces"
                               :placeholder-disabled? true}]]]))


(defn email-input [id]
  (let [value (subscribe [::subs/form-field id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "E-mail:"]
     [:input {:className   (css (:input-field styles/styles-map))
              :value       @value
              :on-change   #(dispatch [::events/update-sharing-form id (extract-input-value %)])
              :on-blur     #(dispatch [::events/store-sharing-form-validation-results id (validation/field-validation id (extract-input-value %))])
              :type        "text"
              :placeholder "xxxx@xxxx.xxx"}]
     (if (not valid?) [:p.text-danger {:className (css (:error styles/styles-map))} "Molimo vas proverite vasu email adresu i pokusajte ponovo."])]))


(defn phone-number-input [id]
  (let [value (subscribe [::subs/form-field id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Telefon:"]
     [:input {:className   (css (:input-field styles/styles-map))
              :value       @value
              :on-change   #(dispatch [::events/update-sharing-form id (extract-input-value %)])
              :type        "text"
              :on-blur     #(dispatch [::events/store-sharing-form-validation-results id (validation/field-validation id (extract-input-value %))])
              :placeholder "06x-xxxx-xxxx"}]
     (if (not valid?) [:p.text-danger {:className (css (:error styles/styles-map))} "Molimo vas proverite vas broj telefona i pokusajte ponovo."] "")]))


(defn post-toggle [id]
  (let [value (subscribe [::subs/form-field id])
        [css] (styles/use-styletron)]
    [:div.form-group {:className (css (:input-wrapper styles/styles-map))}
     [:label {:className (css (:label styles/styles-map))} "Razmena postom?"]
     [:input {:className (css (:input-field styles/styles-map))
              :on-change #(dispatch [::events/update-sharing-form id (extract-checkbox-state %)])
              :type      "checkbox"
              :checked   @value}]]))


(defn pick-up-toggle [id]
  (let [value (subscribe [::subs/form-field id])
        [css] (styles/use-styletron)]
    [:div.form-group {:className (css (:input-wrapper styles/styles-map))}
     [:label {:className (css (:label styles/styles-map))} "Razmena uzivo?"]
     [:input {:className (css (:input-field styles/styles-map))
              :on-change #(dispatch [::events/update-sharing-form id (extract-checkbox-state %)])
              :type      "checkbox"
              :checked   @value}]]))

(defn qty-input [id]
  (let [value (subscribe [::subs/form-field id])
        valid? (subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Koju kolicinu delite?"]
     [:input {:className   (css (:input-field styles/styles-map))
              :value       @value
              :on-change   #(dispatch [::events/update-sharing-form id (long (extract-input-value %))])
              :on-blur     #(dispatch [::events/store-sharing-form-validation-results id (validation/field-validation id (extract-input-value %))])
              :type        "number"
              :min         "1"
              :max         "100"
              :placeholder "1-100"}]
     (if (not @valid?) [:p.text-danger {:className (css (:error styles/styles-map))} "Molimo proverite unetu kolicinu, vrednost mora biti izmedju 1 - 100."])]))



(defn form []
  (let [form-data @(subscribe [::subs/form-data])
        grains-kind @(subscribe [::subs/grains-kind])
        is-valid? @(subscribe [::subs/is-valid? [:firstname :lastname :region :quantity]])
        [css] (styles/use-styletron)]
    [:div {:className (css (:form-wrapper styles/styles-map))}
     [:div {:className (css (:wrapper-title styles/styles-map))} "Kreirajte vas oglas"]
     [:form
      [:div.form-group
       [first-name-input :firstname]
       [last-name-input :lastname]
       [region-select :region]
       [:div.mt-5
        [:p {:className (css (:p styles/styles-map))}
         "Kako da vas zainteresovani kontaktiraju? (telefon ili imejl adresa je obavezna)"]
        [phone-number-input :phone-number]
        [email-input :email]]
       [:div.mt-5
        [:p {:className (css (:p styles/styles-map))} "Kako ćete deliti zrnca? (jedan način deljenja je obavezan)"]
        [post-toggle :post]
        [pick-up-toggle :pick-up]]
       [qty-input :quantity]]]
     [:div {:className (css (:input-field styles/styles-map))}
      [:button.btn.btn-outline-primary
       {:className (css (:btn styles/styles-map))
        :disabled (not is-valid?)
        :on-click #(dispatch [::events/create-ad grains-kind form-data])} "Sačuvaj"]
      [:button.btn.btn-outline-primary
       {:className (css (:btn styles/styles-map))
        :on-click  #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Početna stranica"]]]))


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


(defn ads-list
  "List of all ads."
  []
  (let [selected-region @(subscribe [::subs/seeking-region])
        ads @(subscribe [::subs/filtered-ads])
        grains-kind @(subscribe [::subs/grains-kind])
        [css] (styles/use-styletron)]
    [:div.d-flex.flex-column.min-vh-100.align-items-center
     [:button.btn.btn-outline-primary.col-md-5.mb-5
      {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Početna stranica"]
     [:div
      [:label " Opština: "]
      [:div {:className (css {:width "140pt"})}
       [inputs/search-selector {:placeholder "Izaberite mesto"
                                :options (map (fn [r]
                                                {:title (name r)
                                                 :title-cleaned (utils/replace-serbian-characters (name r))
                                                 :value r
                                                 :on-click (fn [event]
                                                             (dispatch [::events/set-seeking-region
                                                                        (keyword (extract-input-value event))]))})
                                           r/regions)
                                :active-value selected-region
                                :aria-labelledby "learning-spaces"
                                :placeholder-disabled? true}]
       [:button.btn.btn-outline-primary.mt-5.col-12
        {:on-click #(dispatch [::events/fetch-ads selected-region grains-kind])
         :disabled (nil? selected-region)}
        "Pretraži"]]]
     (when (seq ads)
       [:div.table-responsive.mt-5
        [:table.table.table-striped.table-bordered
         [:thead.thead-dark
          [:tr [:th {:scope "col"} "Ime"]
           [:th {:scope "col"} "Prezime"]
           [:th {:scope "col"} "Region"]
           [:th {:scope "col"} "Telefon"]
           [:th {:scope "col"} "E-mail"]
           [:th {:scope "col"} "Slanje poštom"]
           [:th {:scope "col"} "Lično preuzimanje"]]]
         [:tbody
          (user-row ads selected-region)]]])]))


(defn home []
  [:div.d-flex.flex-column.min-vh-100.justify-content-center.align-items-center
   [:h1 "Da li delite ili tražite kefir?"]
   [:button.btn.btn-outline-primary.col-md-5.mb-5.mt-5 {:on-click #(dispatch [::events/ad-type {:type :sharing}])} "Delim"]
   [:button.btn.btn-outline-primary.col-md-5 {:on-click #(dispatch [::events/ad-type {:type :seeking}])} "Tražim"]])


(defn grains-kind []
  [:div.d-flex.flex-column.min-vh-100.justify-content-center.align-items-center
   [:button.btn.btn-outline-primary.col-md-5.mb-5.mt-5 {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :milk-type} "Mlečni"]
   [:button.btn.btn-outline-primary.col-md-5.mb-5 {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :water-type} "Vodeni"]
   [:button.btn.btn-outline-primary.col-md-5 {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :kombucha} "Kombuha"]])


(defn ad-type-choice []
  (case @(subscribe [::subs/ad-type-choice])
    :sharing form
    :seeking ads-list))

(defn thank-you []
  [:div.d-flex.flex-column.min-vh-100.justify-content-center.align-items-center [:h1.mb-5 "Hvala vam sto delite kefir zrnca"]
   [:button.btn.btn-outline-success.mb-5 {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Početna stranica"]])

(defn error []
  [:div.d-flex.flex-column.min-vh-100.justify-content-center.align-items-center
   [:h1 "Trenutno nema korisnika koji dele zrnca u izabranom regionu."]
   [:button.btn.btn-outline-warning {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Početna stranica"]])

;; FORM

(defn- panels [panel-name]
  (case (:name (:data panel-name))
    :route/home [home]
    ;; -----
    :route/ad-type [grains-kind]
    ;; -----
    :route/ad-type-choice [ad-type-choice]
    ;; -----
    :route/thank-you [thank-you]
    ;; -----
    :route/error [error]
    [:div]))

(defn main-panel []
  (let [active-panel @(subscribe [::subs/active-route])]
    [:div.container
     ;; NAVBAR
     [:nav.navbar.navbar-expand-lg.navbar-light.bg-light
      [:a.navbar-brand {:href "/"} "Kefir na Dar"]
      [:ul.navbar-nav
       ; more menu items can be added here
       [:li.nav-item.active
        [:a.nav-link {:href "/"} "Početna"]]]]
     ;; CONTENT
     [panels active-panel]
     ;; FOOTER
     [:p.copyright-text "Copyright © 2022 All Rights Reserved by Do Brave Software"]]))
