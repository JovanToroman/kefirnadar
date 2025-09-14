(ns kefirnadar.application.views
  (:require
    [cuerdas.core :as str]
    [kefirnadar.application.events :as events]
    [kefirnadar.application.subscriptions :as subs]
    [kefirnadar.application.styles :as styles]
    [kefirnadar.application.inputs :as inputs]
    [kefirnadar.application.validation :as validation]
    [re-frame.core :refer [dispatch dispatch-sync subscribe]]
    [kefirnadar.application.regions :as regions]
    [kefirnadar.application.utils.transformations :as transform]
    [kefirnadar.common.utils :refer-macros [-m]]
    [kefirnadar.application.auth :as auth]
    [kefirnadar.application.pagination :as pagination]
    [kefirnadar.application.ui-components :as components]
    [reitit.frontend.easy :as rfe]))

(defn region-select [id]
  (let [selected-region @(subscribe [::subs/form-field id])
        valid? @(subscribe [::subs/form-validation id])
        [css] (styles/use-styletron)]
    [:div.form-group
     [:label {:className (css {:width "200px"
                               :color "#757575"
                               :margin-right "10px"
                               :font-size "40px"})}
      "Mesto:"]
     [:div {:className (css (:custom-select styles/styles-map))}
      [inputs/search-selector {:placeholder "Molimo izaberite mesto"
                               :options (map (fn [r]
                                               {:title (name r)
                                                ;; Koristimo prečišćeni naslov kako bismo pretragu učinili robusnijom
                                                :title-cleaned (transform/replace-serbian-characters (name r))
                                                :value r
                                                :on-click (fn [event]
                                                            (dispatch [::events/update-sharing-form id
                                                                       (inputs/extract-input-value event)]))})
                                          regions/regions)
                               :active-value selected-region
                               :placeholder-disabled? true}]]
     (when (false? valid?)
       [:p.text-danger {:className (css {:width "100%"
                                         :outline "none"
                                         :font-size "20px"
                                         :margin-top "30px"
                                         :transition "all 0.3s ease"})} "Molimo da izaberete region"])]))

(defn email-input [kljuc-roditelja kljuc-polja]
  (let [value (kljuc-polja @(subscribe [::subs/form-field kljuc-roditelja]))
        valid? @(subscribe [::subs/form-validation kljuc-roditelja])]
    (inputs/imejl {:vrednost value
                   :on-change #(dispatch [::events/update-sharing-form kljuc-roditelja
                                          {kljuc-polja (inputs/extract-input-value %)}])
                   :tekst-greske "Molimo vas da unesete ispravan način kontakta"
                   :ispravno? valid?})))

(defn nacin-deljenja [kljuc-roditelja]
  (let [vrednost-postom (:slanje? @(subscribe [::subs/form-field kljuc-roditelja]))
        vrednost-licno (:preuzimanje? @(subscribe [::subs/form-field kljuc-roditelja]))
        potvrdjeno? @(subscribe [::subs/form-validation kljuc-roditelja])
        [css] (styles/use-styletron)]
    [:<>
     [inputs/checkbox "Razmena poštom?" vrednost-postom #(dispatch [::events/update-sharing-form kljuc-roditelja
                                                                    {:slanje? (inputs/extract-checkbox-state %)}])]

     [inputs/checkbox "Razmena lično?" vrednost-licno #(dispatch [::events/update-sharing-form kljuc-roditelja
                                                                  {:preuzimanje? (inputs/extract-checkbox-state %)}])]
     (when (false? potvrdjeno?)
       [:p.text-danger {:className (css (:error styles/styles-map))}
        "Molimo da izaberete makar jednu opciju razmene zrnaca"])]))

(defn vrsta-kulture [kljuc-roditelja]
  (let [value-milk (:deli-mlecni? @(subscribe [::subs/form-field kljuc-roditelja]))
        value-water (:deli-vodeni? @(subscribe [::subs/form-field kljuc-roditelja]))
        value-kombucha (:deli-kombucu? @(subscribe [::subs/form-field kljuc-roditelja]))
        on-change-factory (fn [id] #(dispatch [::events/update-sharing-form kljuc-roditelja
                                               {id (inputs/extract-checkbox-state %)}]))
        potvrdjeno? @(subscribe [::subs/form-validation kljuc-roditelja])
        [css] (styles/use-styletron)]
    [:<>
     [inputs/checkbox "Mlečni" value-milk (on-change-factory :deli-mlecni?)]
     [inputs/checkbox "Vodeni" value-water (on-change-factory :deli-vodeni?)]
     [inputs/checkbox "Kombuha" value-kombucha (on-change-factory :deli-kombucu?)]
     (when (false? potvrdjeno?)
       [:p.text-danger.mt-3 {:className (css {:font-size "20px"
                                              :transition "all 0.3s ease"})}
        "Molimo da izaberete makar jednu vrstu zrnaca"])]))

(defn trenutno-polje-za-unos [{:keys [sadrzaj prethodno trenutno naredno]}]
  (let [[css] (styles/use-styletron)
        vrednost-trenutnog-polja @(subscribe [::subs/form-field trenutno])
        podaci-oglasa @(subscribe [::subs/sharing-form-data])
        id-korisnika @(subscribe [::auth/user-id])]
    [:<>
     [:div {:className (css {:height "350px" :width "300px" :display "flex" :align-items "center"})} sadrzaj]
     [:div {:className (css {:align-items "center"})}
      [inputs/dugme {:oznaka "Prethodno"
                     :class-name (css {:width "130px"})
                     :on-click #(dispatch [::events/promeni-polje-za-unos-oglasa prethodno])
                     :onemoguceno? (nil? prethodno)
                     :velicina :velika}]
      (if (nil? naredno)
        [inputs/dugme {:oznaka "Sačuvaj"
                       :class-name (css {:float "right" :width "130px"})
                       :on-click (fn [_]
                                   (let [polje-potvrdjeno? (validation/potvrdi-vrednost-polja trenutno
                                                             vrednost-trenutnog-polja)]
                                     (if polje-potvrdjeno?
                                       (do
                                         (dispatch [::events/oznaci-polje-kao-potvrdjeno trenutno])
                                         (dispatch [::events/validate-and-create-ad podaci-oglasa id-korisnika]))
                                       (dispatch [::events/oznaci-polje-kao-nepotvrdjeno trenutno]))))
                       :velicina :velika}]
        [inputs/dugme {:oznaka "Naredno"
                       :class-name (css {:float "right" :width "130px"})
                       :on-click (fn [_]
                                   (let [polje-potvrdjeno? (validation/potvrdi-vrednost-polja trenutno
                                                             vrednost-trenutnog-polja)]
                                     (if polje-potvrdjeno?
                                       (do
                                         (dispatch-sync [::events/oznaci-polje-kao-potvrdjeno trenutno])
                                         (dispatch-sync [::events/promeni-polje-za-unos-oglasa naredno]))
                                       (dispatch [::events/oznaci-polje-kao-nepotvrdjeno trenutno]))))
                       :onemoguceno? (nil? naredno)
                       :velicina :velika}])]]))

(defn dodaj-oglas-forma []
  (let [[css] (styles/use-styletron)
        broj-telefona-form-id :broj-telefona
        trenutno-polje @(subscribe [::subs/trenutno-polje-za-unos-oglasa])]
    [:div.mt-3
     (case trenutno-polje
       :oblast [trenutno-polje-za-unos {:sadrzaj [region-select :oblast]
                                        :naredno :kontakt
                                        :trenutno trenutno-polje}]
       :kontakt [trenutno-polje-za-unos
                 {:sadrzaj [:div
                            [:p {:className (css {:font-size "20px"
                                                  :color "#757575"})}
                             (str "Kako da vas zainteresovani kontaktiraju? (telefon ili imejl "
                               "adresa, jedno je obavezno)")]
                            [inputs/broj-telefona
                             {:vrednost (broj-telefona-form-id @(subscribe [::subs/form-field :kontakt]))
                              :on-change #(dispatch [::events/update-sharing-form :kontakt
                                                     {broj-telefona-form-id (inputs/extract-input-value %)}])}]
                            [email-input :kontakt :imejl]]
                  :prethodno :oblast
                  :trenutno trenutno-polje
                  :naredno :nacin-deljenja}]
       :nacin-deljenja [trenutno-polje-za-unos {:sadrzaj [:div
                                                          [:p {:className (css {:font-size "20px"
                                                                                :color "#757575"
                                                                                :margin-bottom "50px"})}
                                                           "Kako ćete deliti zrnca? (jedan način deljenja je obavezan)"]
                                                          [nacin-deljenja :nacin-deljenja]]
                                                :prethodno :kontakt
                                                :trenutno trenutno-polje
                                                :naredno :vrsta-kulture}]
       :vrsta-kulture [trenutno-polje-za-unos {:sadrzaj [:div
                                                         [:p {:className (css {:font-size "20px"
                                                                               :color "#757575"
                                                                               :margin-bottom "50px"})}
                                                          "Koju vrstu zrnaca delite? (jedna vrsta zrnaca je obavezna)"]
                                                         [vrsta-kulture :vrsta-kulture]]
                                               :trenutno trenutno-polje
                                               :prethodno :nacin-deljenja}])]))

(defn format-grains-kinds [sharing_milk_type sharing_water_type sharing_kombucha]
  (str/join ", " (cond-> []
                   sharing_milk_type (conj "mlečni kefir")
                   sharing_water_type (conj "vodeni kefir")
                   sharing_kombucha (conj "kombuhu"))))

(defn broj-telefona-prikaz [show-broj-telefona? broj-telefona ad-id]
  (if show-broj-telefona?
    [:strong.ml-1.mr-1 broj-telefona]
    [:button.btn.btn-sm.btn-info.ml-1.mr-1.mb-1
     {:on-click
      #(dispatch [::events/set-ads-meta ad-id :show-broj-telefona? true])}
     "Prikaži broj"]))

(defn imejl-prikaz [show-email? email ad-id]
  (if show-email?
    [:strong.ml-1.mr-1 email]
    [:button.btn.btn-sm.btn-info.ml-1.mr-1
     {:on-click
      #(dispatch [::events/set-ads-meta ad-id :show-email? true])}
     "Prikaži imejl adresu"]))

(defn oglas
  [css]
  (fn [{:ad/keys [send_by_post share_in_person region sharing_milk_type sharing_water_type sharing_kombucha ad_id
                  broj_telefona imejl]
        :korisnik/keys [korisnicko_ime]}]
    (let [show-broj-telefona? @(subscribe [::subs/ads-meta ad_id :show-broj-telefona?])
          show-email? @(subscribe [::subs/ads-meta ad_id :show-email?])]
      [:div.col-md-12.card.mb-4.p-4 {:key (random-uuid)
                                      :className (css {:line-height 2})}
       (when (some? korisnicko_ime)
         [:h3.row korisnicko_ime])
       [:p "Ovaj delilac deli " [:strong.ml-1.mr-1
                                     (format-grains-kinds sharing_milk_type sharing_water_type sharing_kombucha)
                                     (cond
                                       (and send_by_post share_in_person) " ličnim preuzimanjem i poštom,"
                                       send_by_post " samo poštom,"
                                       share_in_person " samo ličnim preuzimanjem,")]
        "nalaze se u mestu " [:strong.ml-1.mr-1 region] " i možete ih kontaktirati "
        (cond
          (and (not (str/blank? imejl)) (not (str/blank? broj_telefona)))
          [:<> "telefonom na " [broj-telefona-prikaz show-broj-telefona? broj_telefona ad_id]
           " ili elektronskom poštom na " [imejl-prikaz show-email? imejl ad_id]]

          (not (str/blank? broj_telefona)) [:<> "telefonom na "
                                            [broj-telefona-prikaz show-broj-telefona? broj_telefona ad_id]]
          (not (str/blank? imejl)) [:<> "elektronskom poštom na " [imejl-prikaz show-email? imejl ad_id]])]])))

(defn akciona-dugmad-moj-oglas [css id-oglasa]
  [:div.row
   ;; TODO: maybe implement editing
   #_[:button.btn.btn-primary.mr-2 {:className (css {:width "140pt"})
                                  :on-click #(dispatch [::events/izmeni-oglas id-oglasa])}
    "Izmeni oglas"]

   [:button.btn.btn-danger {:className (css {:width "140pt"})
                                  :on-click #(dispatch [::events/izbrisi-oglas id-oglasa])}
    "Izbriši oglas"]])

(defn moj-oglas [css]
  (fn [{:ad/keys [ad_id send_by_post share_in_person region sharing_milk_type sharing_water_type sharing_kombucha
                  broj_telefona imejl created_on quantity]}]
    [:div.col-md-8.card.mb-4.p-4 {:key (random-uuid)
                                  :className (css {:line-height 2})}

     [:div.row
      [:label {:className (css (:label styles/styles-map)) :for "datum-objave"} "Datum objave"]
      [:p {:id "datum-objave"} (transform/format-date created_on)]]

     (when (some? imejl)
       [:div.row
        [:label {:className (css (:label styles/styles-map)) :for "imejl-adresa"} "Imejl adresa"]
        [:p {:id "imejl-adresa"} imejl]])

     (when (some? broj_telefona)
       [:div.row
        [:label {:className (css (:label styles/styles-map)) :for "broj-telefona"} "Broj telefona"]
        [:p {:id "broj-telefona"} broj_telefona]])

     [:div.row
      [:label {:className (css (:label styles/styles-map)) :for "način-slanja"} "Način slanja"]
      [:p {:id "način-slanja"} (str/join ", "
                                 (cond-> []
                                   (true? send_by_post) (conj "Slanje poštom")
                                   (true? share_in_person) (conj "Lično preuzimanje")))]]

     [:div.row
      [:label {:className (css (:label styles/styles-map)) :for "kultura-koju-delim"} "Kultura koju delim"]
      [:p {:id "kultura-koju-delim"} (str/join ", "
                                       (cond-> []
                                         (true? sharing_milk_type) (conj "Zrnca mlečnog kefira")
                                         (true? sharing_water_type) (conj "Zrnca vodenog kefira")
                                         (true? sharing_kombucha) (conj "SCOBY za kombuhu")))]]

     [:div.row
      [:label {:className (css (:label styles/styles-map)) :for "količina"} "Količina"]
      [:p {:id "količina"} quantity]]

     [:div.row
      [:label {:className (css (:label styles/styles-map)) :for "opština"} "Opština"]
      [:p {:id "opština"} region]]

     [akciona-dugmad-moj-oglas css ad_id]]))

(defn region-filter []
  (let [[css] (styles/use-styletron)
        selected-regions @(subscribe [::subs/filters :regions])]
    [:<>
     [:label {:className (css {:width "200px"
                               :color "#757575"
                               :margin-right "10px"
                               :font-size "25px"})}
      " Mesto: "]
     [:div.mb-3 {:className (css {:width "230pt"})}
      [inputs/search-selector {:placeholder "Molimo izaberite mesto"
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
  (let [oglasi @(subscribe [::subs/filtered-ads])
        broj-oglasa @(subscribe [::subs/ads-count])
        {:keys [page-number page-size]} @(subscribe [::subs/ads-pagination-info])
        show-filters? @(subscribe [::subs/show-filters?])
        filters @(subscribe [::subs/filters])
        [css] (styles/use-styletron)]
    [:div.d-flex.flex-column.min-vh-100.align-items-center
     [:button.btn.btn-secondary.mt-5.mb-3 {:on-click #(dispatch [::events/store-show-filters show-filters?])}
      "Filteri"]
     (when show-filters?
       [filters-view])
     [:h3 "Broj oglasa: " broj-oglasa]
     (cond
       ;; iz nekog razloga ne mozemo koristiti css direktno unutar komponenata
       (seq oglasi) [:div.col-md-8 (doall (map (oglas css) oglasi))
                     (pagination/pagination
                       {:change-page-redirect-url-fn (fn [page-number page-size]
                                                       (rfe/href :route/trazim {} (merge
                                                                                    {:page-number page-number
                                                                                     :page-size page-size}
                                                                                    filters)))
                        :page-number page-number
                        :page-size page-size
                        :total-count broj-oglasa
                        :label "Paginacija oglasa"})]
       (some? oglasi) [:p "Trenutno niko ne deli zrnca sa izabranim filterima. Probajte da promenite filtere."]
       :else "Greška")]))


(defn home []
  [:<>
   [:h1 "Da li želite da podelite ili dobijete kefirna zrnca?"]
   [:button.btn.btn-outline-primary.col-md-5.mb-5.mt-5
    {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/delim}}])}
    "Delim"]
   [:button.btn.btn-outline-primary.col-md-5
    {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/trazim}}])}
    "Tražim"]])

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

(defn politika-privatnosti
  []
  [:div.container
   [:div#en.tab-content.translations-content-item.en.visible
    [:h1 "Politika privatnosti za Kefir na dar"]
    [:p "Poslednje ažuriranje: 20. avgust 2025."]
    [:p "Ova Politika privatnosti opisuje Naše politike i procedure o prikupljanju, korišćenju i otkrivanju Vaših podataka kada koristite Uslugu i obaveštava Vas o Vašim pravima na privatnost i kako Vas zakon štiti."]
    [:p "Koristimo Vaše lične podatke da bismo pružili i poboljšali Uslugu. Korišćenjem Usluge, pristajete na prikupljanje i korišćenje informacija u skladu sa ovom Politikom privatnosti."]
    [:h2 "Tumačenje i definicije"]
    [:h3 "Tumačenje"]
    [:p "Reči čije je početno slovo veliko imaju značenja definisana pod sledećim uslovima. Sledeće definicije imaju isto značenje bez obzira da li se pojavljuju u jednini ili u množini."]
    [:h3 "Definicije"]
    [:p "Za potrebe ove Politike privatnosti:"]
    [:ul
     [:li
      [:p [:strong "Nalog"] "označava jedinstveni nalog kreiran za Vas da biste pristupili našoj Usluzi ili delovima naše Usluge."]]
     [:li
      [:p [:strong "Povezano lice"] "označava entitet koji kontroliše, koji je kontrolisan od strane ili je pod zajedničkom kontrolom sa stranom, gde „kontrola“ znači vlasništvo nad 50% ili više akcija, udela u kapitalu ili drugih hartija od vrednosti koje daju pravo glasa za izbor direktora ili drugog upravljačkog organa."]]
     [:li
      [:p [:strong "Kompanija"] "(naziva se „Kompanija“, „Mi“, „Nas“ ili „Naš“ u ovom Ugovoru) odnosi se na Kefir na dar."]]
     [:li
      [:p [:strong "Kolačići"] "su male datoteke koje veb-sajt postavlja na vaš računar, mobilni uređaj ili bilo koji drugi uređaj, a koje sadrže detalje vaše istorije pregledanja na tom veb-sajtu, između ostalog i za njegove brojne upotrebe."]]
     [:li
      [:p [:strong "Zemlja"] "se odnosi na: Srbiju"]]
     [:li
      [:p [:strong "Uređaj"] "znači bilo koji uređaj koji može da pristupi Usluzi, kao što je računar, mobilni telefon ili digitalni tablet."]]
     [:li
      [:p [:strong "Lični podaci"] "su sve informacije koje se odnose na identifikovanu ili identifikovanu osobu."]]
     [:li
      [:p [:strong "Usluga"] "se odnosi na Veb-sajt."]]
     [:li
      [:p [:strong "Pružalac usluga"] "znači svako fizičko ili pravno lice koje obrađuje podatke u ime Kompanija. Odnosi se na kompanije ili pojedince treće strane koje zapošljava Kompanija da olakšaju Uslugu, da obezbede Uslugu u ime Kompanije, da obavljaju usluge vezane za Uslugu ili da pomognu Kompaniji u analizi načina korišćenja Usluge."]]
     [:li
      [:p [:strong "Usluga društvenih medija treće strane"] "odnosi se na bilo koju veb stranicu ili bilo koju veb stranicu društvene mreže preko koje se Korisnik može prijaviti ili kreirati nalog da bi koristio Uslugu."]]
     [:li
      [:p [:strong "Podaci o korišćenju"] "odnose se na podatke prikupljene automatski, bilo generisane korišćenjem Usluge ili iz same infrastrukture Usluge (na primer, trajanje posete stranici)."]]
     [:li
      [:p [:strong "Veb stranica"] "odnosi se na Kefir na dar, dostupan sa" [:a {:href "https://www.termsfeed.com/live/kefirnadar.rs" :target "_blank" :rel "external nofollow noopener"} "kefirnadar.rs"]]]
     [:li
      [:p [:strong "Vi"] "označava pojedinca koji pristupa ili koristi Uslugu, ili kompaniju ili drugo pravno lice u čije ime taj pojedinac pristupa ili koristi Uslugu, shodno onome što je primenljivo."]]]
    [:h2 "Prikupljanje i korišćenje vaših ličnih podataka"]
    [:h3 "Vrste prikupljenih podataka"]
    [:h4 "Lični podaci"]
    [:p "Dok koristite našu Uslugu, možemo vas zamoliti da nam pružite određene lične podatke koji se mogu koristiti za kontaktiranje ili identifikaciju. Lični podaci mogu da uključuju, ali nisu ograničeni na:"]
    [:ul
     [:li
      [:p "E-adresa"]]
     [:li
      [:p "Ime i prezime"]]
     [:li
      [:p "Broj telefona"]]
     [:li
      [:p "Podaci o korišćenju"]]]
    [:h4 "Podaci o korišćenju"]
    [:p "Podaci o korišćenju se automatski prikupljaju prilikom korišćenja Usluge."]
    [:p "Podaci o korišćenju mogu da uključuju informacije kao što su adresa internet protokola vašeg uređaja (npr. IP adresa), tip pregledača, verzija pregledača, stranice naše Usluge koje posećujete, vreme i datum vaše posete, vreme provedeno na tim stranicama, jedinstveni identifikatori uređaja i drugi dijagnostički podaci."]
    [:p "Kada pristupate Usluzi putem mobilnog uređaja, možemo automatski prikupljati određene informacije, uključujući, ali ne ograničavajući se na, tip mobilnog uređaja koji koristite, jedinstveni ID vašeg mobilnog uređaja, IP adresu adresa Vašeg mobilnog uređaja, Vaš mobilni operativni sistem, tip mobilnog internet pregledača koji koristite, jedinstveni identifikatori uređaja i drugi dijagnostički podaci."]
    [:p "Takođe možemoprikupljati informacije koje Vaš pregledač šalje kad god posetite našu Uslugu ili kada pristupate Usluzi putem mobilnog uređaja."]
    [:h4 "Informacije sa društvenih mreža treće strane"]
    [:p "Kompanija Vam omogućava da kreirate nalog i prijavite se da biste koristili Uslugu putem sledećih društvenih mreža treće strane:"]
    [:ul
     [:li "Google"]
     [:li "Facebook"]
     [:li "Instagram"]
     [:li "Twitter"]
     [:li "LinkedIn"]]
    [:p "Ako odlučite da se registrujete putem ili nam na drugi način odobrite pristup društvenoj mreži treće strane, možemo prikupljati lične podatke koji su već povezani sa nalogom Vaše Društvene mreže treće strane, kao što su Vaše ime, Vaša adresa e-pošte, Vaše aktivnosti ili Vaša lista kontakata povezana sa tim nalogom."]
    [:p "Takođe možete imati mogućnost da delite dodatne informacije sa Kompanijom putem naloga Vaše Društvene mreže treće strane. Ako odlučite da pružite takve informacije i lične podatke, tokom registracije ili na drugi način, dajete Kompaniji dozvolu da ih koristi, deli i čuva na način koji je u skladu sa ovom Politikom privatnosti."]
    [:h4 "Tehnologije praćenja i kolačići"]
    [:p "Koristimo kolačiće i slične tehnologije praćenja da bismo pratili aktivnosti na našoj Usluzi i čuvali određene informacije. Tehnologije praćenja koje se koriste su svetionici, oznake i skripte za prikupljanje i praćenje informacija i za poboljšanje i analizu naše Usluge. Tehnologije koje koristimo mogu uključivati:"]
    [:ul
     [:li [:strong "Kolačići ili kolačići pregledača."] "&nbsp;Kolačić je mala datoteka koja se postavlja na vaš uređaj. Možete dati uputstvo svom pregledaču da odbije sve kolačiće ili da naznači kada se kolačić šalje. Međutim, ako ne prihvatite kolačiće, možda nećete moći da koristite neke delove naše Usluge. Osim ako niste podesili podešavanje Vašeg pregledača tako da odbija kolačiće, naša Usluga može da koristi kolačiće."]
     [:li [:strong "Veb svetionici."] "&nbsp;Određeni delovi naše Usluge i naši imejlovi mogu da sadrže male elektronske datoteke poznate kao veb svetionici (takođe poznati kao prozirni gifovi, pikselne oznake i jednopikselni gifovi) koji omogućavaju Kompaniji, na primer, da broji korisnike koji su posetili te stranice ili otvorili imejl i za druge povezane statistike veb stranice (na primer, beleženje popularnosti određenog odeljka i provera integriteta sistema i servera)."]]
    [:p "Kolačići mogu biti „trajni“ ili „sesijski“ kolačići. Trajni kolačići ostaju na Vašem ličnom računaru ili mobilnom uređaju kada ste van mreže, dok se sesijski kolačići brišu čim zatvorite Vaš veb pregledač. Više o kolačićima možete saznati u članku" [:a {:href "https://www.termsfeed.com/blog/cookies/#What_Are_Cookies" :target "_blank"} "TermsFeed veb-sajta"] "."]
    [:p "Koristimo i sesijske i trajne kolačiće u svrhe navedene u nastavku:"]
    [:ul
     [:li
      [:p [:strong "Neophodni / Esencijalni kolačići"]]
      [:p "Tip: Sesijski kolačići"]
      [:p "Administrator: Nas"]
      [:p "Svrha: Ovi kolačići su neophodni da bi vam se pružile usluge dostupne putem veb-sajta i da bi vam se omogućilo da koristite neke od njegovih funkcija. Oni pomažu u autentifikaciji korisnika i sprečavanju prevarne upotrebe korisničkih naloga. Bez ovih kolačića, usluge koje ste tražili ne mogu biti pružene, a mi koristimo ove kolačiće samo da bismo vam pružili te usluge."]]
     [:li
      [:p [:strong "Politika kolačića / Obaveštenje o prihvatanju kolačića"]]
      [:p "Tip: Trajni kolačići"]
      [:p "Administrator: Mi"]
      [:p "Svrha: Ovi kolačići identifikuju da li su korisnici prihvatili upotrebu kolačića na veb-sajtu."]]
     [:li
      [:p [:strong "Funkcionalni kolačići"]]
      [:p "Tip: Trajni kolačići"]
      [:p "Administrator: Mi"]
      [:p "Svrha: Ovi kolačići nam omogućavaju da zapamtimo izbore koje pravite kada koristite veb-sajt, kao što je pamćenje vaših podataka za prijavu ili jezičkih preferencija. Svrha ovih kolačića je da vam pruže personalizovanije iskustvo i da vas izbegnu da ponovo unosite svoje postavke svaki put kada koristite veb lokaciju."]]]
    [:p "Za više informacija o kolačićima koje koristimo i vašim izborima u vezi sa kolačićima, posetite našu Politiku kolačića ili odeljak Kolačići u našoj Politici privatnosti."]
    [:h3 "Korišćenje vaših ličnih podataka"]
    [:p "Kompanija može koristiti lične podatke u sledeće svrhe:"]
    [:ul
     [:li
      [:p [:strong "Da bismo pružili i održavali našu Uslugu"] ", uključujući praćenje korišćenja naše Usluge."]]
     [:li
      [:p [:strong "Da bismo upravljali vašim nalogom:"] "&nbsp;da bismo upravljali vašom registracijom kao korisnika Usluge. Lični podaci koje pružate mogu vam omogućiti pristup različitim funkcionalnostima Usluge koje su vam dostupne kao registrovanom korisniku."]]
     [:li
      [:p [:strong "Za izvršenje ugovora:"] "razvoj, usklađenost i sprovođenje ugovora o kupovini proizvoda, predmeta ili usluga koje ste kupili ili bilo kog drugog ugovora sa nama putem Usluge."]]
     [:li
      [:p [:strong "Da bismo vas kontaktirali:"] "Da bismo vas kontaktirali putem e-pošte, telefonskih poziva, SMS-a ili drugih ekvivalentnih oblika e-pošteelektronska komunikacija, kao što su push obaveštenja mobilne aplikacije u vezi sa ažuriranjima ili informativne komunikacije u vezi sa funkcionalnostima, proizvodima ili ugovorenim uslugama, uključujući bezbednosna ažuriranja, kada je to neophodno ili razumno za njihovu implementaciju."]]
     [:li
      [:p [:strong "Da bismo vam pružili"] "vesti, posebne ponude i opšte informacije o drugim proizvodima, uslugama i događajima koje nudimo, a koji su slični onima koje ste već kupili ili o kojima ste se raspitivali, osim ako niste odlučili da ne primate takve informacije."]]
     [:li
      [:p [:strong "Da bismo upravljali vašim zahtevima:"] "Da bismo odgovorili na vaše zahteve upućene nama i upravljali njima."]]
     [:li
      [:p [:strong "Za poslovne transfere:"] "Možemo koristiti vaše podatke za procenu ili sprovođenje spajanja, prodaje, restrukturiranja, reorganizacije, raspuštanja ili druge prodaje ili prenosa dela ili svih naših sredstava, bilo kao stalno poslovanje ili kao deo stečaja, likvidacije ili sličnog postupka, u kojem su lični podaci koje posedujemo o korisnicima naših usluga među sredstvima preneto."]]
     [:li
      [:p [:strong "U druge svrhe"] ": Možemo koristiti Vaše podatke u druge svrhe, kao što su analiza podataka, identifikovanje trendova korišćenja, određivanje efikasnosti naših promotivnih kampanja i za procenu i poboljšanje naše Usluge, proizvoda, usluga, marketinga i vašeg iskustva."]]]
    [:p "Možemo deliti Vaše lične podatke u sledećim situacijama:"]
    [:ul
     [:li [:strong "Sa Pružaocima usluga:"] "&nbsp;Možemo deliti Vaše lične podatke sa Pružaocima usluga radi praćenja i analize korišćenja naše Usluge, kako bismo Vas kontaktirali."]
     [:li [:strong "Za poslovne transfere:"] "&nbsp;Možemo deliti ili preneti Vaše lične podatke u vezi sa ili tokom pregovora o bilo kakvom spajanju, prodaji imovine Kompanije, finansiranju ili akviziciji celog ili dela Našeg poslovanja drugoj kompaniji."]
     [:li [:strong "Sa Pridruženim licima:"] "&nbsp;Možemo deliti Vaše podatke sa Našim Pridruženim Licima, u kom slučaju ćemo zahtevati od tih Pridruženih Lica da poštuju ovu Politiku privatnosti. Pridružena lica uključuju našu matičnu kompaniju i sve druge podružnice, partnere u zajedničkom ulaganju ili druge kompanije koje kontrolišemo ili koje su pod zajedničkom kontrolom sa nama."]
     [:li [:strong "Sa poslovnim partnerima:"] "&nbsp;Možemo deliti vaše podatke sa našim poslovnim partnerima kako bismo vam ponudili određene proizvode, usluge ili promocije."]
     [:li [:strong "Sa drugim korisnicima:"] "&nbsp;Kada delite lične podatke ili na drugi način komunicirate u javnim prostorima sa drugim korisnicima, takve informacije mogu videti svi korisnici i mogu biti javno distribuirane van njih. Ako komunicirate sa drugim korisnicima ili se registrujete putem usluge društvenih medija treće strane, vaši kontakti na usluzi društvenih medija treće strane mogu videti vaše ime, profil, slike i opis vaše aktivnosti. Slično tome, drugi korisnici će moći da vide opise Vaših aktivnosti, komuniciraju sa Vama i vide Vaš profil."]
     [:li [:strong "Uz Vaš pristanak"] ": Možemo otkriti Vaše lične podatke u bilo koju drugu svrhu uz Vaš pristanak."]]
    [:h3 "Čuvanje Vaših ličnih podataka"]
    [:p "Kompanija će čuvati Vaše Lične podatke samo onoliko dugo koliko je potrebno za svrhe navedene u ovoj Politici privatnosti. Čuvati ćemo i koristiti Vaše Lične podatke u meri u kojoj je to neophodno da bismo ispunili naše zakonske obaveze (na primer, ako smo dužni da čuvamo vaše podatke da bismo se pridržavali važećih zakona), rešavali sporove i sprovodili naše pravne sporazume i politike."]
    [:p "Kompanija će takođe čuvati Podatke o korišćenju za potrebe interne analize. Podaci o korišćenju se generalno čuvaju kraći vremenski period, osim kada se ovi podaci koriste za jačanje bezbednosti ili poboljšanje funkcionalnosti naše usluge, ili kada smo zakonski obavezni da čuvamo ove podatke duže vreme."]
    [:h3 "Prenos vaših ličnih podataka"]
    [:p "Vaši podaci, uključujući lične podatke, obrađuju se u operativnim kancelarijama Kompanije i na svim drugim mestima gde se nalaze strane uključene u obradu. To znači da se ovi podaci mogu preneti na — i održavati na — računari koji se nalaze van Vaše države, pokrajine, zemlje ili druge vladine jurisdikcije gde se zakoni o zaštiti podataka mogu razlikovati od onih iz Vaše jurisdikcije."]
    [:p "Vaš pristanak na ovu Politiku privatnosti, nakon čega sledi Vaše dostavljanje takvih informacija, predstavlja Vaš pristanak na taj prenos."]
    [:p "Kompanija će preduzeti sve razumno neophodne korake kako bi osigurala da se sa Vašim podacima postupa bezbedno i u skladu sa ovom Politikom privatnosti i da se Vaši lični podaci neće prenositi organizaciji ili zemlji osim ako nisu na snazi odgovarajuće kontrole, uključujući bezbednost Vaših podataka i drugih ličnih informacija."]
    [:h3 "Izbrišite svoje lične podatke"]
    [:p "Imate pravo da izbrišete ili zatražite da nam pomognemo u brisanju ličnih podataka koje smo prikupili oVi."]
    [:p "Naša Usluga vam može pružiti mogućnost da obrišete određene informacije o sebi iz same Usluge."]
    [:p "Možete ažurirati, izmeniti ili obrisati svoje informacije u bilo kom trenutku tako što ćete se prijaviti na svoj nalog, ako ga imate, i posetiti odeljak podešavanja naloga koji vam omogućava da upravljate svojim ličnim podacima. Takođe nas možete kontaktirati da biste zatražili pristup, ispravili ili obrisali bilo koje lične podatke koje ste nam dali."]
    [:p "Međutim, imajte na umu da ćemo možda morati da zadržimo određene informacije kada imamo zakonsku obavezu ili zakonski osnov za to."]
    [:h3 "Otkrivanje vaših ličnih podataka"]
    [:h4 "Poslovne transakcije"]
    [:p "Ako je Kompanija uključena u spajanje, akviziciju ili prodaju imovine, vaši lični podaci mogu biti preneti. Obavestićemo vas pre nego što se vaši lični podaci prenesu i postanu predmet druge Politike privatnosti."]
    [:h4 "Sprovođenje zakona"]
    [:p "Pod određenim okolnostima, Kompanija može biti obavezna da otkrije vaše lične podatke ako je to potrebno po zakonu ili kao odgovor na valjane zahteve javnih organa (npr. suda ili vladine agencije)."]
    [:h4 "Drugi zakonski zahtevi"]
    [:p "Kompanija može otkriti vaše lične podatke u dobroj veri da je takva radnja neophodna za:"]
    [:ul
     [:li "Poštovanje zakonske obaveze"]
     [:li "Zaštitu i odbranu prava ili imovine Kompanije"]
     [:li "Sprečavanje ili istraživanje mogućih zloupotreba u vezi sa Uslugom"]
     [:li "Zaštitu lične bezbednosti Korisnika Usluge ili javnosti"]
     [:li "Zaštitu od pravne odgovornosti"]]
    [:h3 "Bezbednost vaših ličnih podataka"]
    [:p "Bezbednost vaših ličnih podataka nam je važna, ali imajte na umu da nijedan način prenosa preko Internet ili metod elektronskog skladištenja je 100% bezbedan. Iako se trudimo da koristimo komercijalno prihvatljiva sredstva za zaštitu vaših ličnih podataka, ne možemo garantovati njihovu apsolutnu bezbednost."]
    [:h2 "Privatnost dece"]
    [:p "Naša Usluga nije namenjena nikome mlađem od 13 godina. Ne prikupljamo svesno lične podatke od bilo koga mlađeg od 13 godina. Ako ste roditelj ili staratelj i svesni ste da nam je vaše dete pružilo lične podatke, kontaktirajte nas. Ako saznamo da smo prikupili lične podatke od bilo koga mlađeg od 13 godina bez provere roditeljskog pristanka, preduzimamo korake da uklonimo te informacije sa naših servera."]
    [:p "Ako moramo da se oslonimo na saglasnost kao pravni osnov za obradu vaših podataka i vaša zemlja zahteva saglasnost roditelja, možemo zahtevati saglasnost vašeg roditelja pre nego što prikupimo i koristimo te informacije."]
    [:h2 "Linkovi do drugih veb lokacija"]
    [:p "Naša Usluga može da sadrži linkove do drugih veb lokacija kojima ne upravljamo mi. Ako kliknete na link treće strane, bićete usmereni na sajt te treće strane. Toplo vam savetujemo da pregledate Politiku privatnosti svakog sajta koji posetite."]
    [:p "Nemamo kontrolu nad i ne preuzimamo nikakvu odgovornost za sadržaj, politike privatnosti ili prakse bilo kojih sajtova ili usluga trećih strana."]
    [:h2 "Izmene ove Politike privatnosti"]
    [:p "Možemo povremeno ažurirati našu Politiku privatnosti. Obavestićemo vas o svim promenama objavljivanjem nove Politike privatnosti na ovoj stranici."]
    [:p "Obavestićemo vas putem e-pošte i/ili istaknutog obaveštenja na našem servisu, pre nego što promena stupi na snagu i ažuriraćemo datum „Poslednje ažuriranje“ na vrhu ove Politike privatnosti."]
    [:p "Savetujemo vam da periodično pregledate ovu Politiku privatnosti za sve promene. Izmene ove Politike privatnosti stupaju na snagu kada se objave na ovoj stranici."]
    [:h2 "Kontaktirajte nas"]
    [:p "Ako imate bilo kakvih pitanja o ovoj Politici privatnosti, možete nas kontaktirati:"]
    [:ul
     [:li
      [:p "E-poštom:&nbsp;info@kefirnadar.rs"]]
     [:li
      [:p "Posetom ove stranice na našoj veb stranici:&nbsp;" [:a {:href "https://kefirnadar.rs/kontakt" :target "_blank" :rel "external nofollow noopener"} "https://kefirnadar.rs/kontakt"]]]]]])

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
      [:form.col-md-6.align-items-center {:on-submit #(dispatch-sync [::auth/prijava {:imejl imejl :lozinka lozinka} %])}
       [inputs/imejl {:vrednost imejl
                      :on-change #(dispatch [::events/azuriraj-formu-prijave :imejl (inputs/extract-input-value %)])
                      :tekst-greske "Unesite ispravnu imejl adresu"
                      :ispravno? imejl-validan?}]
       [inputs/lozinka {:vrednost lozinka
                        :on-change #(dispatch [::events/azuriraj-formu-prijave :lozinka (inputs/extract-input-value %)])}]
       [inputs/dugme {:tip "submit"
                      :oznaka "Prijavi se"}]
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
    (let [imejl @(subscribe [::subs/polje-forme-za-slanje-imejla-za-resetovanje-lozinke :imejl])
          imejl-validan @(subscribe [::subs/provera-forme-za-slanje-imejla-za-resetovanje-lozinke :imejl])]
      [:div.col-md-6
       [inputs/imejl {:vrednost imejl
                      :on-change #(dispatch [::events/azuriraj-formu-za-slanje-imejla-za-resetovanje-lozinke
                                             :imejl (inputs/extract-input-value %)])
                      :tekst-greske "Unesite ispravnu imejl adresu"
                      :ispravno? imejl-validan}]
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

(defn kontakt-strana []
  (let [imejl @(subscribe [::subs/polje-kontakt-forme :imejl])
        imejl-ispravan? @(subscribe [::subs/provera-polja-kontakt-forme :imejl])
        poruka @(subscribe [::subs/polje-kontakt-forme :poruka])
        poruka-ispravna? @(subscribe [::subs/provera-polja-kontakt-forme :poruka])]
    [:div.col-md-6
     [:h1.mb-5 "Napišite nam poruku"]
     [inputs/imejl {:vrednost imejl
                    :on-change #(dispatch [::events/azuriraj-kontakt-formu :imejl (inputs/extract-input-value %)])
                    :tekst-greske "Unesite ispravnu imejl adresu"
                    :ispravno? imejl-ispravan?}]
     [inputs/text-area {:vrednost poruka
                        :on-change #(dispatch [::events/azuriraj-kontakt-formu :poruka (inputs/extract-input-value %)])
                        :tekst-greske "Poruka ne sme biti prazna i mora sadržati barem 20 karaktera."
                        :ispravno? poruka-ispravna?
                        :natpis "Poruka: "
                        :placeholder "Ovde napišite vašu poruku"}]
     [inputs/dugme {:oznaka "Pošalji poruku"
                    :on-click #(dispatch [::events/posalji-kontakt-poruku {:imejl imejl :poruka poruka}])}]]))

(defn nakon-slanja-kontakt-poruke []
  [:h1 "Uspešno ste poslali poruku."])

(defn moji-oglasi []
  (let [[css] (styles/use-styletron)
        moji-oglasi @(subscribe [::subs/moji-oglasi])]
    (if (seq moji-oglasi)
      [:div.col-md-12.d-flex.flex-column.min-vh-100.align-items-center
       (doall (map (moj-oglas css) moji-oglasi))]
      [:div
       [:h1 "Nemate nijedan postavljen oglas. Zašto ne postavite jedan sada?"]
       [:button.btn.btn-outline-primary.col-md-5.mb-5.mt-5
        {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/delim}}])}
        "Podeli zrnca"]])))

(defn stranica-za-prijavu []
  [:div
   [:h1.mb-5.text-center "Prijavljivanje"]
   ;; fb prijava privremeno onesposobljena
   [:button.btn.btn-primary.col-md-12.mb-3.mr-3
    {:on-click #(auth/log-user-in (:facebook auth/auth-methods))}
    [:i.fa-brands.fa-facebook.fa-xl] " Prijavi se pomoću Fejsbuka"]
   [:button.btn.btn-secondary.col-md-12.mb-3
    {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/prijava-osnovna}}])}
    "Prijavi se pomoću imejla i lozinke"]
   [:button.btn.btn-outline-secondary.col-md-12
    {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/registracija}}])}
    "Napravi novi nalog"]])

(defn- panels [panel-name]
  (case panel-name
    :route/home [home]
    :route/delim [dodaj-oglas-forma]
    :route/trazim [ads-list]
    :route/thank-you [thank-you]
    :route/error [error]
    :route/politika-privatnosti [politika-privatnosti]
    :route/registracija [registracija-korisnika]
    :route/aktiviraj-korisnika [aktiviraj-korisnika]
    :route/prijava [stranica-za-prijavu]
    :route/prijava-osnovna [prijava-korisnika]
    :route/nakon-registracije [nakon-registracije]
    :route/slanje-imejla-za-resetovanje-lozinke [slanje-imejla-za-resetovanje-lozinke]
    :route/nakon-slanja-imejla-za-resetovanje-lozinke [nakon-slanja-imejla-za-resetovanje-lozinke]
    :route/resetovanje-lozinke [resetovanje-lozinke]
    :route/nakon-resetovanja-lozinke [nakon-resetovanja-lozinke]
    :route/kontakt [kontakt-strana]
    :route/nakon-slanja-kontakt-poruke [nakon-slanja-kontakt-poruke]
    :route/moji-oglasi [moji-oglasi]
    [:div]))

(defn main-panel []
  (let [{{panel-name :name _public? :public?} :data} @(subscribe [::subs/active-route])
        [css] (styles/use-styletron)
        authenticated? @(subscribe [::auth/authenticated?])
        {:keys [ime prezime email]} @(subscribe [::auth/user-info])
        identifikator-korisnika (or ime prezime email)
        ;; temporarily don't require authentication
        authentication-required? false                      ;;(and (not authenticated?) (not public?)) onesposobio sam prijavu radi veće upotrebe aplikacije
        ]
    [:div.container
     [:nav.navbar.navbar-expand-lg.navbar-light
      [:div.container-fluid
       [:a.navbar-brand {:href "/"} "Kefir na Dar"]
       [:ul.navbar-nav
        [:li.nav-item
         [:a.nav-link.mr-2 {:href "/kontakt"} "Kontakt"]]
        [:li.nav-item
         (if authenticated?
           `(~(when-not (str/blank? identifikator-korisnika)
               [:a.nav-link "Prijavljeni ste kao " [:strong identifikator-korisnika]])
             [:a.nav-link.mr-2 {:href "/moji-oglasi" :key "moji-oglasi"} "Moji oglasi"]
             [:a.nav-link {:href "/odjava" :key "odjavi-me"} "Odjavi me"])
           [:a.nav-link {:href "/prijava"} "Prijavi se"])]]]]
     [:div.d-flex.flex-column.justify-content-center.align-items-center
      {:className (css {:min-height "80vh"})}
      (if authentication-required?
        [stranica-za-prijavu]
        [panels panel-name authenticated?])]
     [:p.copyright-text.mt-5.d-flex.justify-content-center "Copyright © 2022-2025 All Rights Reserved by Do Brave Plus Software"]]))
