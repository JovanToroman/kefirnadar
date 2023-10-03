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
    [kefirnadar.application.ui-components :as components]
    [reitit.frontend.easy :as rfe]))

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

(defn email-input [id]
  (let [value @(subscribe [::subs/form-field id])
        valid? @(subscribe [::subs/form-validation id])]
    (inputs/imejl {:vrednost value
                   :on-change #(dispatch [::events/update-sharing-form id (inputs/extract-input-value %)])
                   :tekst-greske "Molimo vas da unesete ispravan način kontakta"
                   :ispravno? valid?})))

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
  (let [form-info @(subscribe [::subs/sharing-form-data])
        [css] (styles/use-styletron)
        id-korisnika @(subscribe [::auth/user-id])
        broj-telefona-form-id :phone-number]
    [:div.col-md-8.mt-3
     [:h1 "Kreirajte vaš oglas"]
     [:form
      [:div.form-group
       [region-select :region]
       [:div.mt-5
        [:p {:className (css (:p styles/styles-map))}
         "Kako da vas zainteresovani kontaktiraju? (telefon ili imejl adresa je obavezna)"]
        [inputs/broj-telefona {:vrednost @(subscribe [::subs/form-field broj-telefona-form-id])
                               :on-change #(dispatch [::events/update-sharing-form broj-telefona-form-id
                                                      (inputs/extract-input-value %)])}]
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
        :on-click #(dispatch [::events/validate-and-create-ad form-info id-korisnika])} "Sačuvaj"]
      [:button.btn.btn-outline-primary
       {:className (css (:btn styles/styles-map))
        :on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Početna stranica"]]]))

(defn format-grains-kinds [sharing_milk_type sharing_water_type sharing_kombucha]
  (str/join ", " (cond-> []
                   sharing_milk_type (conj "mlečni kefir")
                   sharing_water_type (conj "vodeni kefir")
                   sharing_kombucha (conj "kombuhu"))))

(defn phone-number [show-phone-number? phone-number ad-id]
  (if show-phone-number?
    [:strong.ml-1.mr-1 phone-number]
    [:button.btn.btn-sm.btn-info.ml-1.mr-1.mb-1
     {:on-click
      #(dispatch [::events/set-ads-meta ad-id :show-phone-number? true])}
     "Prikaži broj"]))

(defn prikaz-imejla [show-email? email ad-id]
  (if show-email?
    [:strong.ml-1.mr-1 email]
    [:button.btn.btn-sm.btn-info.ml-1.mr-1
     {:on-click
      #(dispatch [::events/set-ads-meta ad-id :show-email? true])}
     "Prikaži imejl adresu"]))

(defn ad-row
  [css {:ad/keys [send_by_post share_in_person region sharing_milk_type sharing_water_type sharing_kombucha ad_id]
        :korisnik/keys [korisnicko_ime phone_number email]}]
  (let [show-phone-number? @(subscribe [::subs/ads-meta ad_id :show-phone-number?])
        show-email? @(subscribe [::subs/ads-meta ad_id :show-email?])]
    [:div.col-md-10.card.mb-4.pl-4.pt-4 {:key (random-uuid)
                                         :className (css {:line-height 2})}
     [:h3.row korisnicko_ime]
     [:p.row "Ovaj delilac deli " [:strong.ml-1.mr-1
                                   (format-grains-kinds sharing_milk_type sharing_water_type sharing_kombucha)
                                   (cond
                                     (and send_by_post share_in_person) " ličnim preuzimanjem i poštom,"
                                     send_by_post " samo poštom,"
                                     share_in_person " samo ličnim preuzimanjem,")]
      "nalaze se u mestu " [:strong.ml-1.mr-1 region] " i možete ih kontaktirati "
      (cond
        (and (not (str/empty-or-nil? email)) (not (str/empty-or-nil? phone_number)))
        [:<> "telefonom na " [phone-number show-phone-number? phone_number ad_id]
         "ili elektronskom poštom na " [prikaz-imejla show-email? email ad_id]]

        (not (str/empty-or-nil? phone_number)) [:<> "telefonom na "
                                         [phone-number show-phone-number? phone_number ad_id]]
        (not (str/empty-or-nil? email)) [:<> "elektronskom poštom na " [prikaz-imejla show-email? email ad_id]])]]))

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
        filters @(subscribe [::subs/filters])
        [css] (styles/use-styletron)]
    [:div.d-flex.flex-column.min-vh-100.align-items-center
     [:button.btn.btn-secondary.mt-5.mb-3 {:on-click #(dispatch [::events/store-show-filters show-filters?])}
      "Filteri"]
     (when show-filters?
       [filters-view])
     [:h3 "Broj oglasa: " ads-count]
     (cond
       ;; iz nekog razloga ne mozemo koristiti css direktno unutar komponenata
       (seq ads) [:div (into [:<>] (map (partial ad-row css)) ads)
                  (pagination/pagination
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
   [:button.btn.btn-outline-warning {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])}
    "Početna stranica"]])

(defn privacy-policy
  []
  [:div
   [:h1 "Politika privatnosti"]
   [:p "Ovo je naša politika privatnosti"]])

(defn greska-registracije [kod-greske]
  (let [[css] (styles/use-styletron)]
    [:p.text-danger {:className (css (:error styles/styles-map))}
     (case kod-greske
       :imejl-vec-iskoriscen "Korisnik sa ovom imejl adresom je već registrovan. Pokušajte da se prijavite."
       :korisnicko-ime-zauzeto "Korisničko ime je zauzeto. Probajte drugo korisničko ime."
       nil)]))

(defn registracija-korisnika
  []
  (if (not @(subscribe [::auth/authenticated?]))
    (let [korisnicko-ime @(subscribe [::subs/polje-forme-registracije :korisnicko-ime])
          korisnicko-ime-validno? @(subscribe [::subs/provera-forme-registracije :korisnicko-ime])
          imejl @(subscribe [::subs/polje-forme-registracije :imejl])
          imejl-validan? @(subscribe [::subs/provera-forme-registracije :imejl])
          lozinka @(subscribe [::subs/polje-forme-registracije :lozinka])
          lozinka-validna? @(subscribe [::subs/provera-forme-registracije :lozinka])
          kod-greske @(subscribe [::subs/kod-greske :registracija])]
      [:div.col-md-6
       [inputs/korisnicko-ime {:vrednost korisnicko-ime
                               :on-change #(dispatch [::events/azuriraj-formu-registracije :korisnicko-ime
                                                      (inputs/extract-input-value %)])
                               :tekst-greske "Unesite ime od makar 5 karaktera"
                               :ispravno? korisnicko-ime-validno?}]
       [inputs/imejl {:vrednost imejl
                      :on-change #(dispatch [::events/azuriraj-formu-registracije :imejl (inputs/extract-input-value %)])
                      :tekst-greske "Unesite ispravnu imejl adresu"
                      :ispravno? imejl-validan?}]
       [inputs/lozinka {:vrednost lozinka
                        :on-change #(dispatch [::events/azuriraj-formu-registracije :lozinka (inputs/extract-input-value
                                                                                               %)])
                        :tekst-greske "Unesite kompleksniju lozinku sa minimum osam karaktera koja sadrži makar jedan
                       broj, jedno veliko slovo i jedan poseban karakter"
                        :ispravno? lozinka-validna?}]
       [inputs/dugme {:oznaka "Registruj se"
                      :on-click #(dispatch [::auth/dodaj-korisnika {:imejl imejl
                                                                    :lozinka lozinka
                                                                    :korisnicko-ime korisnicko-ime}])}]
       [greska-registracije kod-greske]])
    "Već ste prijavljeni. Ne možete se registrovati opet dok se ne odjavite."))

(defn greska-prijave [kod-greske]
  (let [[css] (styles/use-styletron)]
    [:p.text-danger {:className (css (:error styles/styles-map))}
     (case kod-greske
       :korisnik-ne-postoji "Neuspešna prijava. Proverite da li ste uneli ispravnu imejl adresu."
       :korisnik-nije-aktiviran (str "Vaš nalog nije aktiviran. Molimo proverite sanduče elektronske pošte i pratite "
                                  "vezu za aktivaciju naloga.")
       :pogresna-lozinka "Pogrešna lozinka"
       kod-greske)]))

(defn prijava-korisnika
  []
  (if (not @(subscribe [::auth/authenticated?]))
    (let [imejl @(subscribe [::subs/polje-forme-prijave :imejl])
          imejl-validan? @(subscribe [::subs/provera-forme-prijave :imejl])
          lozinka @(subscribe [::subs/polje-forme-prijave :lozinka])
          kod-greske @(subscribe [::subs/kod-greske :prijava])
          aktivacioni-kod-poslat? @(subscribe [::subs/aktivacioni-kod-poslat?])]
      [:div.col-md-6.align-items-center
       [inputs/imejl {:vrednost imejl
                      :on-change #(dispatch [::events/azuriraj-formu-prijave :imejl (inputs/extract-input-value %)])
                      :tekst-greske "Unesite ispravnu imejl adresu"
                      :ispravno? imejl-validan?}]
       [inputs/lozinka {:vrednost lozinka
                        :on-change #(dispatch [::events/azuriraj-formu-prijave :lozinka (inputs/extract-input-value %)])}]
       [inputs/dugme {:oznaka "Prijavi se"
                      :on-click #(dispatch [::auth/prijava {:imejl imejl
                                                            :lozinka lozinka}])}]
       [:a.link-primary.row {:href (rfe/href :route/slanje-imejla-za-resetovanje-lozinke {} (when (not (str/blank? imejl))
                                                                                          {:imejl imejl}))}
        "Zaboravili ste lozinku?"]
       [:a.link-primary.row {:href (rfe/href :route/registracija)}
        "Nemate nalog? Registrujte se!"]
       [greska-prijave kod-greske]
       (when (= kod-greske :korisnik-nije-aktiviran)
         (if-not aktivacioni-kod-poslat?
           [inputs/dugme {:oznaka "Ponovo pošalji registracioni kod na svoju imejl adresu"
                          :on-click #(dispatch [::auth/posalji-aktivacioni-kod imejl])}]
           [:p "Aktivacioni kod je poslat na vašu imejl adresu"]))])
    [:div.alert.alert-primary {:role "alert"} "Već ste prijavljeni. Ne možete se prijaviti opet dok se ne odjavite."]))

(defn- greska-aktiviranja [kod-greske]
  (let [[css] (styles/use-styletron)]
    [:p.text-danger {:className (css (:error styles/styles-map))}
     (case kod-greske
       :aktivacioni-kod-neispravan "Veza za aktivaciju je neispravna."
       kod-greske)]))

(defn aktiviraj-korisnika []
  (if (not @(subscribe [::auth/authenticated?]))
    (let [kod-greske @(subscribe [::subs/kod-greske :aktiviraj-korisnika])]
      [:<>
       [:h1 "Molimo sačekajte dok aktiviramo vaš nalog \uD83D\uDC86"]
       [components/spinner 10]
       [greska-aktiviranja kod-greske]])
    [:div.alert.alert-primary {:role "alert"} "Već ste prijavljeni. Ne možete aktivirati nalog dok se ne odjavite."]))

(defn nakon-registracije []
  [:h1 "Molimo posetite vaše imejl sanduče i pratite link za aktivaciju naloga koji smo vam poslali"])

(defn slanje-imejla-za-resetovanje-lozinke
  []
  (if (not @(subscribe [::auth/authenticated?]))
    (let [imejl @(subscribe [::subs/polje-forme-za-slanje-imejla-za-resetovanje-lozinke :imejl])]
      [:div.col-md-6
       [inputs/imejl {:vrednost imejl
                      :on-change #(dispatch [::events/azuriraj-formu-za-slanje-imejla-za-resetovanje-lozinke
                                             :imejl (inputs/extract-input-value %)])
                      :tekst-greske "Unesite ispravnu imejl adresu"}]
       [inputs/dugme {:oznaka "Pošalji imejl za resetovanje lozinke"
                      :on-click #(dispatch [::auth/posalji-imejl-za-resetovanje-lozinke imejl])}]])
    [:div.alert.alert-primary {:role "alert"} "Već ste prijavljeni. Ne možete resetovati lozinku dok se ne odjavite."]))

(defn nakon-slanja-imejla-za-resetovanje-lozinke []
  [:h1 "Ukoliko ste uneli ispravnu imejl adresu, u sanduče će vam stići imejl sa vezom za resetovanje lozinke."])

(defn greska-resetovanja-lozinke [kod-greske]
  (let [[css] (styles/use-styletron)]
    [:p.text-danger {:className (css (:error styles/styles-map))}
     (case kod-greske
       :kod-za-resetovanje-lozinke-neispravan "Neispravan kod za resetovanje lozinke"
       :pogresna-lozinka "Stara lozinka koju ste uneli je neispravna"
       kod-greske)]))

(defn resetovanje-lozinke
  []
  (let [kod-za-resetovanje-lozinke @(subscribe [::subs/polje-forme-za-resetovanje-lozinke :kod-za-resetovanje-lozinke])
        nova-lozinka @(subscribe [::subs/polje-forme-za-resetovanje-lozinke :nova-lozinka])
        nova-lozinka-validna? @(subscribe [::subs/provera-forme-za-resetovanje-lozinke :nova-lozinka])
        kod-greske @(subscribe [::subs/kod-greske :resetovanje-lozinke])]
    [:div.col-md-6
     [inputs/lozinka {:vrednost nova-lozinka
                      :on-change #(dispatch [::events/azuriraj-formu-za-resetovanje-lozinke
                                             :nova-lozinka (inputs/extract-input-value %)])
                      :tekst-greske "Unesite kompleksniju lozinku sa minimum osam karaktera koja sadrži makar jedan
                       broj, jedno veliko slovo i jedan poseban karakter"
                      :ispravno? nova-lozinka-validna?
                      :natpis "Nova lozinka: "}]
     [inputs/dugme {:oznaka "Resetuj lozinku"
                    :on-click #(dispatch [::auth/resetuj-lozinku kod-za-resetovanje-lozinke (-m nova-lozinka)])}]
     [greska-resetovanja-lozinke kod-greske]]))

(defn nakon-resetovanja-lozinke []
  [:h1 "Uspešno ste resetovali lozinku."])

(defn- panels [panel-name]
  (case panel-name
    :route/home [home]
    :route/sharing [share-grains-form]
    :route/seeking [ads-list]
    :route/thank-you [thank-you]
    :route/error [error]
    :route/privacy-policy [privacy-policy]
    :route/registracija [registracija-korisnika]
    :route/aktiviraj-korisnika [aktiviraj-korisnika]
    :route/prijava [prijava-korisnika]
    :route/nakon-registracije [nakon-registracije]
    :route/slanje-imejla-za-resetovanje-lozinke [slanje-imejla-za-resetovanje-lozinke]
    :route/nakon-slanja-imejla-za-resetovanje-lozinke [nakon-slanja-imejla-za-resetovanje-lozinke]
    :route/resetovanje-lozinke [resetovanje-lozinke]
    :route/nakon-resetovanja-lozinke [nakon-resetovanja-lozinke]
    [:div]))

(defn login-page []
  [:div
   [:h1.mb-5.text-center "Prijavljivanje"]
   [:button.btn.btn-primary.col-md-12.mb-3
    {:on-click #(auth/log-user-in (:facebook auth/auth-methods))}
    [:i.fa-brands.fa-facebook.fa-xl.mr-3] "Prijavi se pomoću Fejsbuka"]
   [:button.btn.btn-secondary.col-md-12.mb-3
    {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/prijava}}])}
    "Prijavi se pomoću imejla i lozinke"]
   [:button.btn.btn-outline-secondary.col-md-12
    {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/registracija}}])}
    "Napravi novi nalog"]])

(defn main-panel []
  (let [{{panel-name :name public? :public?} :data} @(subscribe [::subs/active-route])
        [css] (styles/use-styletron)
        authenticated? @(subscribe [::auth/authenticated?])
        authentication-required? (and (not authenticated?) (not public?))]
    [:div.container
     ;; NAVBAR
     [:nav.navbar.navbar-expand-lg.navbar-light.bg-light
      [:a.navbar-brand {:href "/"} "Kefir na Dar"]
      [:ul.navbar-nav
       ; more menu items can be added here
       [:li.nav-item.active
        [:a.nav-link {:href "/"} "Početna"]]]
      (when authenticated?
        [:a.ml-auto.navbar-nav {:href "/odjava"} "Odjavi me"])]
     ;; CONTENT
     [:div.d-flex.flex-column.justify-content-center.align-items-center
      {:className (css (:main-panel styles/styles-map))}
      (if authentication-required?
        [login-page]
        [panels panel-name authenticated?])]
     ;; FOOTER
     [:p.copyright-text.mt-5.d-flex.justify-content-center "Copyright © 2022-2023 All Rights Reserved by Do Brave Plus Software"]]))
