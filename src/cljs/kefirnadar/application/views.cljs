(ns kefirnadar.application.views
  (:require [goog.string :as gstr]
            [kefirnadar.application.events :as events]
            [kefirnadar.application.subscriptions :as subs]
            [kefirnadar.application.styles :as styles]
            [kefirnadar.application.inputs :as inputs]
            [re-frame.core :refer [dispatch subscribe]]
            [kefirnadar.application.regions :as regions]
            [applied-science.js-interop :as j]
            [kefirnadar.application.utils :as utils]
            [kefirnadar.application.specs :as specs]))

;; -- helper functions region --
(defn extract-input-value
  [event]
  (j/get-in event [:target :value]))

(defn extract-checkbox-state
  [event]
  (j/get-in event [:target :checked]))


;; -- end helper functions region --

(defn first-name-input [id]
  (let [value @(subscribe [::subs/form-field id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Ime:"]
     [:input
      {:className (css (:input-field styles/styles-map))
       :value value
       :on-change #(dispatch [::events/update-sharing-form id (extract-input-value %)])
       :type "text"
       :required true
       :placeholder "Vase ime..."}]
     (when (and (some? value) (false? valid?))
       [:spam.text-danger {:className (css (:error styles/styles-map))} "Molimo da unesete vaše ime"])]))


(defn last-name-input [id]
  (let [value @(subscribe [::subs/form-field id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Prezime:"]
     [:input {:className (css (:input-field styles/styles-map))
              :value value
              :on-change #(dispatch [::events/update-sharing-form id (extract-input-value %)])
              :type "text"
              :placeholder "Vase prezime..."}]
     (when (and (some? value) (false? valid?))
       [:p.text-danger {:className (css (:error styles/styles-map))} "Molimo da unesete vaše prezime"])]))


(defn region-select [id]
  (let [selected-region (subscribe [::subs/form-field id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Opština:"]
     [:div {:className (css (:custom-select styles/styles-map))}
      [inputs/search-selector {:placeholder "Molimo izaberite mesto"
                               :options (map (fn [r] {:title (name r)
                                                      :value r
                                                      :title-cleaned (utils/replace-serbian-characters (name r))
                                                      :on-click (fn [event]
                                                                  (dispatch [::events/update-sharing-form id
                                                                             (keyword (extract-input-value event))]))})
                                          regions/regions)
                               :active-value @selected-region
                               :placeholder-disabled? true}]]
     (when (false? valid?)
       [:p.text-danger {:className (css (:error styles/styles-map))} "Molimo da izaberete region"])]))

(defn phone-number-input [id]
  (let [value @(subscribe [::subs/form-field id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Telefon:"]
     [:input {:className (css (:input-field styles/styles-map))
              :value value
              :on-change #(dispatch [::events/update-sharing-form id (extract-input-value %)])
              :type "text"
              :placeholder "06x-xxxx-xxxx"}]]))

(defn email-input [id]
  (let [value @(subscribe [::subs/form-field id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Imejl adresa:"]
     [:input {:className (css (:input-field styles/styles-map))
              :value value
              :on-change #(dispatch [::events/update-sharing-form id (extract-input-value %)])
              :type "text"
              :placeholder "xxxx@xxxx.xxx"}]
     (when (and (some? value) (false? valid?))
       [:p.text-danger {:className (css (:error styles/styles-map))}
        "Molimo vas da unesete ispravan način kontakta"])]))

(defn post-toggle [id]
  (let [value (subscribe [::subs/form-field id])
        [css] (styles/use-styletron)]
    [:div.form-group {:className (css (:input-wrapper styles/styles-map))}
     [:label {:className (css (:label styles/styles-map))} "Razmena poštom?"]
     [:input {:className (css (:input-field styles/styles-map))
              :on-change #(dispatch [::events/update-sharing-form id (extract-checkbox-state %)])
              :type "checkbox"
              :checked @value}]]))


(defn pick-up-toggle [id]
  (let [value (subscribe [::subs/form-field id])
        [css] (styles/use-styletron)
        valid? @(subscribe [::subs/form-validation id])]
    [:<>
     [:div.form-group {:className (css (:input-wrapper styles/styles-map))}
      [:label {:className (css (:label styles/styles-map))} "Razmena uživo?"]
      [:input {:className (css (:input-field styles/styles-map))
               :on-change #(dispatch [::events/update-sharing-form id (extract-checkbox-state %)])
               :type "checkbox"
               :checked @value}]]
     (when (and (some? value) (false? valid?))
       [:p.text-danger {:className (css (:error styles/styles-map))}
        "Molimo da izaberete makar jednu opciju razmene zrnaca"])]))

(defn qty-input [id]
  (let [value (subscribe [::subs/form-field id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Koju količinu delite?"]
     [:input {:className (css (:input-field styles/styles-map))
              :value @value
              :on-change #(dispatch [::events/update-sharing-form id (long (extract-input-value %))])
              :type "number"
              :min "1"
              :max "100"
              :placeholder "1-100"}]
     (when (and (some? value) (false? valid?))
       [:p.text-danger {:className (css (:error styles/styles-map))}
        "Molimo proverite unetu količinu, vrednost mora biti između 1 i 100."])]))



(defn share-grains-form []
  (let [form-info @(subscribe [::subs/form-data])
        grains-kind @(subscribe [::subs/grains-kind :sharing])
        [css] (styles/use-styletron)]
    [:div {:className (css (:form-wrapper styles/styles-map))}
     [:div {:className (css (:wrapper-title styles/styles-map))} "Kreirajte vaš oglas"]
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
        [post-toggle :post?]
        [pick-up-toggle :pick-up?]]
       [qty-input :quantity]]]
     [:div {:className (css (:input-field styles/styles-map))}
      [:button.btn.btn-outline-primary
       {:className (css (:btn styles/styles-map))
        :on-click #(dispatch [::events/validate-and-create-ad grains-kind form-info])} "Sačuvaj"]
      [:button.btn.btn-outline-primary
       {:className (css (:btn styles/styles-map))
        :on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Početna stranica"]]]))


;; Ovo cu promeniti, napravio sam ovako samo da bi video da li mi radi..
(defn user-row
  [{:ad/keys [firstname lastname region phone-number email post? pick-up?]}]
  [:tr {:key (random-uuid)}
   [:td firstname]
   [:td lastname]
   [:td region]
   [:td (or phone-number (gstr/unescapeEntities "&#10007"))]
   [:td (or email (gstr/unescapeEntities "&#10007"))]
   [:td (if (some? post?) (gstr/unescapeEntities "&#10004") (gstr/unescapeEntities "&#10007"))]
   [:td (if (some? pick-up?) (gstr/unescapeEntities "&#10004") (gstr/unescapeEntities "&#10007"))]])


(defn ads-list
  "List of all ads."
  []
  (let [selected-region @(subscribe [::subs/seeking-region])
        ads @(subscribe [::subs/filtered-ads])
        grains-kind @(subscribe [::subs/grains-kind :seeking])
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
                                           regions/regions)
                                :active-value selected-region
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
          (map user-row ads)]]])]))


(defn home []
  [:<>
   [:h1 "Da li delite ili tražite kefir?"]
   [:button.btn.btn-outline-primary.col-md-5.mb-5.mt-5
    {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/sharing}}])}
    "Delim"]
   [:button.btn.btn-outline-primary.col-md-5
    {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/seeking}}])}
    "Tražim"]])


(defn grains-kind [^::specs/user-action user-action]
  (let [route-to-load (case user-action
                        :sharing :route/share-grains-form
                        :seeking :route/search-for-grains)]
    [:<>
     [:button.btn.btn-outline-primary.col-md-5.mb-5.mt-5
      {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name route-to-load}
                                                            :path-params {:grains-kind :milk-type}}])
       :value :milk-type}
      "Mlečni"]
     [:button.btn.btn-outline-primary.col-md-5.mb-5
      {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name route-to-load}
                                                            :path-params {:grains-kind :water-type}}])
       :value :water-type}
      "Vodeni"]
     [:button.btn.btn-outline-primary.col-md-5
      {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name route-to-load}
                                                            :path-params {:grains-kind :kombucha}}])
       :value :kombucha}
      "Kombuha"]]))

(defn thank-you []
  [:<>
   [:h1.mb-5 "Hvala vam što delite kefir zrnca"]
   [:button.btn.btn-outline-success.mb-5
    {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])}
    "Početna stranica"]])

(defn error []
  [:<>
   [:h1 "Trenutno nema korisnika koji dele zrnca u izabranom regionu."]
   [:button.btn.btn-outline-warning {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Početna stranica"]])

;; FORM

(defn- panels [panel-name]
  (case (:name (:data panel-name))
    :route/home [home]
    ;; -----
    :route/sharing [grains-kind :sharing]
    :route/seeking [grains-kind :seeking]
    ;; -----
    :route/share-grains-form [share-grains-form]
    :route/search-for-grains [ads-list]
    ;; -----
    :route/thank-you [thank-you]
    ;; -----
    :route/error [error]
    [:div]))

(defn main-panel []
  (let [active-panel @(subscribe [::subs/active-route])
        [css] (styles/use-styletron)]
    [:div.container
     ;; NAVBAR
     [:nav.navbar.navbar-expand-lg.navbar-light.bg-light
      [:a.navbar-brand {:href "/"} "Kefir na Dar"]
      [:ul.navbar-nav
       ; more menu items can be added here
       [:li.nav-item.active
        [:a.nav-link {:href "/"} "Početna"]]]]
     ;; CONTENT
     [:div.d-flex.flex-column.justify-content-center.align-items-center
      {:className (css (:main-panel styles/styles-map))}
      [panels active-panel]]
     ;; FOOTER
     [:p.copyright-text "Copyright © 2022 All Rights Reserved by Do Brave Software"]]))
