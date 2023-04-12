(ns kefirnadar.application.views
  (:require
    [cuerdas.core :as str]
    [kefirnadar.application.events :as events]
    [kefirnadar.application.subscriptions :as subs]
    [kefirnadar.application.styles :as styles]
    [kefirnadar.application.inputs :as inputs]
    [re-frame.core :refer [dispatch subscribe]]
    [kefirnadar.application.regions :as regions]
    [kefirnadar.application.utils.transformations :as transform]
    [kefirnadar.common.utils :refer-macros [-m]]
    [kefirnadar.application.specs :as specs]
    [kefirnadar.application.auth :as auth]
    [kefirnadar.application.pagination :as pagination]
    [reitit.frontend.easy :as rfe]))

(defn first-name-input [id]
  (let [value @(subscribe [::subs/form-field id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Ime:"]
     [:input
      {:className (css (:input-field styles/styles-map))
       :value value
       :on-change #(dispatch [::events/update-sharing-form id (inputs/extract-input-value %)])
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
              :on-change #(dispatch [::events/update-sharing-form id (inputs/extract-input-value %)])
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
                               :options (map (fn [region]
                                               {:title region
                                                :value region
                                                :title-cleaned (transform/replace-serbian-characters region)
                                                :on-click (fn [event]
                                                            (dispatch [::events/update-sharing-form id
                                                                       (inputs/extract-input-value event)]))})
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
              :on-change #(dispatch [::events/update-sharing-form id (inputs/extract-input-value %)])
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
              :on-change #(dispatch [::events/update-sharing-form id (inputs/extract-input-value %)])
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
              :on-change #(dispatch [::events/update-sharing-form id (inputs/extract-checkbox-state %)])
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
               :on-change #(dispatch [::events/update-sharing-form id (inputs/extract-checkbox-state %)])
               :type "checkbox"
               :checked @value}]]
     (when (and (some? value) (false? valid?))
       [:p.text-danger {:className (css (:error styles/styles-map))}
        "Molimo da izaberete makar jednu opciju razmene zrnaca"])]))

(defn form-grains-kinds-toggles []
  (let [value-milk @(subscribe [::subs/form-field :sharing-milk-type?])
        value-water @(subscribe [::subs/form-field :sharing-water-type?])
        value-kombucha @(subscribe [::subs/form-field :sharing-kombucha?])
        on-change-factory (fn [id] #(dispatch [::events/update-sharing-form id (inputs/extract-checkbox-state %)]))
        valid? @(subscribe [::subs/form-validation :sharing-milk-type?])
        [css] (styles/use-styletron)]
    [:<>
     [inputs/checkbox "Mlečni" value-milk (on-change-factory :sharing-milk-type?)]
     [inputs/checkbox "Vodeni" value-water (on-change-factory :sharing-water-type?)]
     [inputs/checkbox "Kombuha" value-kombucha (on-change-factory :sharing-kombucha?)]
     (when (and (some? value-milk) (false? valid?))
       [:p.text-danger {:className (css (:error styles/styles-map))}
        "Molimo da izaberete makar jednu vrstu zrnaca"])]))

(defn qty-input [id]
  (let [value (subscribe [::subs/form-field id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css (:label styles/styles-map))} "Koju količinu delite?"]
     [:input {:className (css (:input-field styles/styles-map))
              :value @value
              :on-change #(dispatch [::events/update-sharing-form id (long (inputs/extract-input-value %))])
              :type "number"
              :min "1"
              :max "100"
              :placeholder "1-100"}]
     (when (and (some? value) (false? valid?))
       [:p.text-danger {:className (css (:error styles/styles-map))}
        "Molimo proverite unetu količinu, vrednost mora biti između 1 i 100."])]))



(defn share-grains-form []
  (let [form-info @(subscribe [::subs/form-data])
        #_#_user-id @(subscribe [::auth/user-id])
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
       [:div.mt-5
        [:p {:className (css (:p styles/styles-map))} "Koju vrstu zrnaca delite? (jedna vrsta zrnaca je obavezna)"]
        [form-grains-kinds-toggles]]
       [qty-input :quantity]]]
     [:div {:className (css (:input-field styles/styles-map))}
      [:button.btn.btn-outline-primary
       {:className (css (:btn styles/styles-map))
        :on-click #(dispatch [::events/validate-and-create-ad form-info #_user-id])} "Sačuvaj"]
      [:button.btn.btn-outline-primary
       {:className (css (:btn styles/styles-map))
        :on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Početna stranica"]]]))

(defn format-grains-kinds [sharing_milk_type sharing_water_type sharing_kombucha]
  (str/join ", " (cond-> []
                   sharing_milk_type (conj "mlečni kefir")
                   sharing_water_type (conj "vodeni kefir")
                   sharing_kombucha (conj "kombuhu"))))

(defn ad-row
  [{:ad/keys [first_name last_name phone_number email send_by_post share_in_person region sharing_milk_type
              sharing_water_type sharing_kombucha ad_id]}]
  (let [show-phone-number? @(subscribe [::subs/ads-meta ad_id :show-phone-number?])]
    [:div.col-md-10.card.mb-4.pl-4.pt-4 {:key (random-uuid)}
     [:h3.row (str/format "%s %s" first_name last_name)]
     [:p.row "Ovaj delilac deli " [:strong.ml-1.mr-1
                                   (format-grains-kinds sharing_milk_type sharing_water_type sharing_kombucha)
                                   (cond
                                     (and send_by_post share_in_person) " ličnim preuzimanjem i poštom,"
                                     send_by_post " samo poštom,"
                                     share_in_person " samo ličnim preuzimanjem,")]
      "nalaze se u mestu " [:strong.ml-1.mr-1 region] " i možete ih kontaktirati "
      (cond
        (and (not (str/blank? email)) (not (str/blank? phone_number)))
        [:<> "telefonom na " (if show-phone-number?
                                           [:strong.ml-1.mr-1 phone_number]
                                           [:button.btn.btn-sm.btn-info.ml-1
                                            {:on-click
                                             #(dispatch [::events/set-ads-meta ad_id :show-phone-number? true])}
                                            "Prikaži broj"])
          "ili elektronskom poštom na " [:strong.ml-1.mr-1 email]]

        (not (str/blank? phone_number)) [:<> "telefonom na "
                                         (if show-phone-number?
                                           [:strong.ml-1.mr-1 phone_number]
                                           [:button.btn.btn-sm.btn-info.ml-1
                                            {:on-click
                                             #(dispatch [::events/set-ads-meta ad_id :show-phone-number? true])}
                                            "Prikaži broj"])]
        (not (str/blank? email)) [:<> "elektronskom poštom na " [:strong.ml-1.mr-1 email]])]]))

(defn region-filter []
  (let [[css] (styles/use-styletron)
        selected-regions @(subscribe [::subs/filters :regions])]
    [:<>
     [:label " Opština: "]
     [:div.mb-3 {:className (css {:width "140pt"})}
      [inputs/search-selector {:placeholder "Izaberite mesto"
                               :options (map (fn [r]
                                               {:title (name r)
                                                ;; we use the cleaned title to make the search more robust
                                                :title-cleaned (transform/replace-serbian-characters (name r))
                                                :value r
                                                :on-click (fn [event]
                                                            (dispatch [::events/update-filters
                                                                       (inputs/extract-input-value event) :regions]))})
                                          regions/regions)
                               ;; TODO: change this to use multiple regions
                               :active-value (first selected-regions)
                               :placeholder-disabled? true}]]]))

(defn grains-kind-filter []
  (let [{:keys [seeking-milk-type? seeking-water-type? seeking-kombucha?]} @(subscribe [::subs/filters])
        on-change-factory (fn [id] #(dispatch [::events/update-filters (inputs/extract-checkbox-state %) id]))]
    [:<>
     [:label "Vrsta zrnaca:"]
     [inputs/checkbox "Mlečni" seeking-milk-type? (on-change-factory :seeking-milk-type?)]
     [inputs/checkbox "Vodeni" seeking-water-type? (on-change-factory :seeking-water-type?)]
     [inputs/checkbox "Kombuha" seeking-kombucha? (on-change-factory :seeking-kombucha?)]]))

(defn delivery-filter []
  (let [{:keys [receive-by-post? receive-in-person?]} @(subscribe [::subs/filters])
        on-change-factory (fn [id] #(dispatch [::events/update-filters (inputs/extract-checkbox-state %) id]))]
    [:<>
     [:label "Način preuzimanja:"]
     [inputs/checkbox "Slanje poštom" receive-by-post? (on-change-factory :receive-by-post?)]
     [inputs/checkbox "Lično preuzimanje" receive-in-person? (on-change-factory :receive-in-person?)]]))

(defn filters-view []
  (let [[css] (styles/use-styletron)
        filters @(subscribe [::subs/filters])]
    [:div.mb-5
     [region-filter]
     [grains-kind-filter]
     [delivery-filter]
     [:button.btn.btn-primary.mr-2 {:className (css {:width "140pt"})
                                    :on-click #(dispatch [::events/apply-filters filters])}
      "Primeni filtere"]
     [:button.btn.btn-outline-primary {:className (css {:width "140pt"})
                                       :on-click #(dispatch [::events/reset-filters])}
      "Resetuj filtere"]]))

(defn ads-list
  "List of all ads."
  []
  (let [ads @(subscribe [::subs/filtered-ads])
        ads-count @(subscribe [::subs/ads-count])
        {:keys [page-number page-size]} @(subscribe [::subs/ads-pagination-info])
        show-filters? @(subscribe [::subs/show-filters?])
        filters @(subscribe [::subs/filters])]
    [:div.d-flex.flex-column.min-vh-100.align-items-center
     [:button.btn.btn-secondary.mt-5.mb-3 {:on-click #(dispatch [::events/store-show-filters show-filters?])}
      "Filteri"]
     (when show-filters?
       [filters-view])
     (cond
       (seq ads) [:div (into [:<>] (map ad-row) ads)
                  (pagination/pagination
                    ;; TODO: zameniti sa rfe/href
                    {:change-page-redirect-url-fn (fn [page-number page-size]
                                                    (rfe/href :route/seeking {} (merge
                                                                                  {:page-number page-number
                                                                                   :page-size page-size}
                                                                                  filters)))
                     :page-number page-number
                     :page-size page-size
                     :total-count ads-count
                     :label "Paginacija oglasa"})]
       (some? ads) [:p "Trenutno niko ne deli zrnca sa izabranim filterima. Probajte da promenite filtere."]
       :else "Greska")]))


(defn home []
  [:<>
   [:h1 "Da li želite da podelite ili dobijete kefirna zrnca?"]
   [:button.btn.btn-outline-primary.col-md-5.mb-5.mt-5
    {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/sharing}}])}
    "Delim"]
   [:button.btn.btn-outline-primary.col-md-5
    {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/seeking}}])}
    "Tražim"]])


#_(defn grains-kind [^::specs/user-action user-action]
  (let [route-to-load (case user-action
                        :sharing :route/share-grains-form
                        :seeking :route/search-for-grains)]
    [:<>
     [:button.btn.btn-outline-primary.col-md-5.mb-5.mt-5
      {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name route-to-load}
                                                            :path-params {:grains-kind "milk-type"}}])
       :value "milk-type"}
      "Mlečni"]
     [:button.btn.btn-outline-primary.col-md-5.mb-5
      {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name route-to-load}
                                                            :path-params {:grains-kind "water-type"}}])
       :value "water-type"}
      "Vodeni"]
     [:button.btn.btn-outline-primary.col-md-5
      {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name route-to-load}
                                                            :path-params {:grains-kind "kombucha"}}])
       :value "kombucha"}
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

(defn privacy-policy
  []
  [:div
   [:h1 "Politika privatnosti"]
   [:p "Ovo je naša politika privatnosti"]])

(defn- panels [panel-name]
  (case panel-name
    :route/home [home]
    :route/sharing [share-grains-form]
    :route/seeking [ads-list]
    :route/thank-you [thank-you]
    :route/error [error]
    :route/privacy-policy [privacy-policy]
    [:div]))

(defn login-page []
  [:div
   [:h1.mb-5 "Prijavljivanje"]
   [:button.btn.btn-primary
    {:on-click #(auth/log-user-in (:facebook auth/auth-methods))}
    [:i.fa-brands.fa-facebook.fa-xl.mr-3] "Prijavite se pomoću Fejsbuka"]])

(defn main-panel []
  (let [{{panel-name :name _public? :public?} :data} @(subscribe [::subs/active-route])
        [css] (styles/use-styletron)
        #_#_#_#_authenticated? @(subscribe [::auth/authenticated?])
        authentication-required? (and (not authenticated?) (not public?))]
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
      #_(if authentication-required?
        [login-page]
        [panels panel-name])
      [panels panel-name]]
     ;; FOOTER
     [:p.copyright-text "Copyright © 2022-2023 All Rights Reserved by Do Brave Plus Software"]]))
