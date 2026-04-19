(ns kefirnadar.application.inputs
  "A place for all UI components which are generic and reusable"
  (:require [applied-science.js-interop :as j]
            [cuerdas.core :as str]
            [kefirnadar.application.events :as events]
            [kefirnadar.application.styles :as styles]
            [kefirnadar.application.subscriptions :as subs]
            [kefirnadar.application.utils.transformations :as transform]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]))

(defn extract-input-value
  [event]
  (j/get-in event [:target :value]))

(defn extract-checkbox-state
  [event]
  (j/get-in event [:target :checked]))

(defn- render-options
  [filtered-options active-value ^atom show-options? css]
  (doall
    (for [{:keys [value title on-click]} filtered-options
          :let [is-active? (= active-value value)]]
      ^{:key value}
      [:button {:class (when is-active? "active")
                              :title title
                              :value (keyword value)
                              :on-click (fn [event]
                                          (on-click event)
                                          (swap! show-options? not))
                              :type "button"
                              :className (str (when is-active? "active") " dropdown-item " (css {:font-size "25px"}))}
       title])))

(defn search-selector-dropdown
  [{:keys [search-input-id]}]
  (r/create-class
    {:component-did-mount (fn [_] (j/call (j/call js/document :getElementById search-input-id) :focus))
     :reagent-render
     (fn [{:keys [search-input-id search-text id css filtered-options active-value show-options?]}]
       [:<>
        [:div
         [:input.col-12.form-control
          {:id search-input-id
           :placeholder "Pretražite pomoću tastature"
           :type "text"
           :on-change (fn [event] (reset! search-text (j/get-in event [:target :value])))
           :value @search-text
           :aria-label "Search"
           :className (css {:font-size "23px"})}]
         [:i.fa.fa-keyboard {:aria-hidden "true"}]]
        [:div.dropdown-menu {:aria-labelledby id :className (css {:display "block"
                                                                  :height :300pt
                                                                  :overflow-y :scroll})}
         (if (seq filtered-options)
           (render-options filtered-options active-value show-options? css)
           [:p {:className (css {:font-size "25px"})} "Nema rezultata"])]])}))

(defn search-selector [_]
  (let [search-text (r/atom "")
        show-options? (r/atom false)]
    (fn [{:keys [options
                 active-value
                 placeholder
                 aria-labelledby
                 id]
          :or {id "dropdownMenuButton1"}}]
      (let [[css] (styles/use-styletron)
            filtered-options (if (or (nil? @search-text) (str/blank? @search-text))
                               options
                               (filter (fn [{:keys [title title-cleaned]}]
                                         (or (re-find
                                               (re-pattern
                                                 (str "(?i)" (transform/remove-reserved-characters @search-text)))
                                               title)
                                           (re-find
                                             (re-pattern
                                               (str "(?i)" (transform/remove-reserved-characters @search-text)))
                                             title-cleaned)))
                                 options))
            {:keys [title]
             :as _active-option} (some (fn [{:keys [value] :as option}]
                                         (when (= value active-value)
                                           option))
                                   options)
            search-input-id (str "search-input" (random-uuid))]
        [:div.dropdown
         [:button.btn.btn-secondary.dropdown-toggle
          (cond-> {:id id
                   :aria-expanded (str @show-options?)
                   :aria-haspopup "true"
                   :type "button"
                   :on-click (fn [_]
                               (reset! search-text "")
                               (swap! show-options? not))
                   :className (css {:position "relative"
                                    :display "flex"
                                    :align-items "center"
                                    :justify-content "space-between"
                                    :padding "13px 15px"
                                    :width "100%"
                                    :border "solid 1px rgba(12, 12, 12, 0.5)"
                                    :font-size "25px"
                                    :line-height "normal"})}
            (seq aria-labelledby) (assoc :aria-labelledby aria-labelledby))

          [:span {:className (css {:font-size "25px"})} (if (some? title)
                                                          title
                                                          placeholder)]]

         (when @show-options?
           [search-selector-dropdown {:search-input-id search-input-id
                                      :search-text search-text
                                      :id id
                                      :css css
                                      :filtered-options filtered-options
                                      :active-value active-value
                                      :show-options? show-options?}])]))))

(defn checkbox [label value on-change]
  (let [[css] (styles/use-styletron)
        check-id (str "check-" (random-uuid))]
    [:div.form-check.form-switch.mb-4 {:className (css {:line-height "150%"
                                                        :margin-left "10px"})}
     [:label.form-check-label {:className (css {:color "#757575"
                                                :font-size "30px"
                                                :margin-left "20px"})
                               :for check-id}
      label]
     [:input.form-check-input {:className (css {:transform "scale(1.5)"})
                               :on-change on-change
                               :type "checkbox"
                               :checked value
                               :id check-id}]]))

(defn imejl [{:keys [vrednost on-change tekst-greske ispravno?]}]
  (let [[css] (styles/use-styletron)]
    [:div.form-group.mb-3
     [:label {:className (css {:color "#757575" :font-size "25px"})} "Imejl adresa:"]
     [:input.form-control.form-control-lg {:value vrednost
                                           :on-change on-change
                                           :type "text"
                                           :placeholder "xxxx@xxxx.xxx"}]
     (when (false? ispravno?)
       [:p.text-danger {:className (css (:error styles/styles-map))}
        tekst-greske])]))

(defn potvrda-imejla [{:keys [vrednost on-change tekst-greske ispravno?]}]
  (let [[css] (styles/use-styletron)]
    [:div.form-group.mb-3
     [:label {:className (css {:color "#757575" :font-size "25px"})} "Potvrdi imejl adresu:"]
     [:input.form-control.form-control-lg {:value vrednost
                                           :on-change on-change
                                           :type "text"
                                           :placeholder "Ponovite unesenu imejl adresu"}]
     (when (false? ispravno?)
       [:p.text-danger {:className (css (:error styles/styles-map))}
        tekst-greske])]))

(defn broj-telefona [{:keys [vrednost on-change]}]
  (let [[css] (styles/use-styletron)]
    [:div.form-group.mb-3
     [:label {:className (css {:color "#757575"
                               :font-size "25px"})} "Broj telefona:"]
     [:input.form-control.form-control-lg {:value vrednost
                                           :on-change on-change
                                           :type "text"
                                           :placeholder "06x-xxxx-xxxx"}]]))

(defn- prikazi-lozinku [lozinka-prikazana? kljuc-unosa]
  (let [[css] (styles/use-styletron)]
    (if lozinka-prikazana?
      [:button.btn {:on-click #(dispatch [::events/prikazi-lozinku kljuc-unosa false])}
       [:img {:src "/ikone/eye-slash.avif"
              :className (css {:width "2em" :height "2em"})}]]
      [:button.btn {:on-click #(dispatch [::events/prikazi-lozinku kljuc-unosa true])}
       [:img {:src "/ikone/eye.avif"
              :className (css {:width "2em" :height "2em"})}]])))

(defn lozinka [{:keys [vrednost on-change tekst-greske ispravno? natpis] :or {natpis "Lozinka: "}}]
  (let [[css] (styles/use-styletron)
        lozinka-prikazana? @(subscribe [::subs/prikazi-lozinku :lozinka])]
    [:div.form-group.mb-3
     [:label {:className (css {:color "#757575" :font-size "25px"})} natpis]
     [:div.d-flex.gap-3
      [:input {:className (css (:input-field styles/styles-map))
               :value vrednost
               :on-change on-change
               :type (if lozinka-prikazana? "text" "password")}]
      [prikazi-lozinku lozinka-prikazana? :lozinka]]
     (when (and (some? vrednost) (false? ispravno?))
       [:p.text-danger {:className (css (:error styles/styles-map))}
        tekst-greske])]))

(defn potvrda-lozinke [{:keys [vrednost on-change tekst-greske ispravno? natpis] :or {natpis "Potvrda lozinke: "}}]
  (let [[css] (styles/use-styletron)
        lozinka-prikazana? @(subscribe [::subs/prikazi-lozinku :potvrda-lozinke])]
    [:div.form-group.mb-3
     [:label {:className (css {:color "#757575" :font-size "25px"})} natpis]
     [:div.d-flex.gap-3
      [:input {:className (css (:input-field styles/styles-map))
               :value vrednost
               :on-change on-change
               :type (if lozinka-prikazana? "text" "password")}]
      [prikazi-lozinku lozinka-prikazana? :potvrda-lozinke]]
     (when (and (some? vrednost) (false? ispravno?))
       [:p.text-danger {:className (css (:error styles/styles-map))}
        tekst-greske])]))

(defn dugme [{:keys [oznaka on-click tip onemoguceno? nivo class-name velicina] :or {tip "button" nivo "prvi-oivicen"}}]
  [:button.mb-3
   (cond-> {:className (cond-> "btn"
                         (= nivo "prvi-oivicen") (str " btn-outline-primary")
                         (= velicina :velika) (str " btn-lg")
                         (some? class-name) (str " " class-name))
            :on-click on-click
            :type tip}
     onemoguceno? (assoc :disabled true))
   oznaka])

(defn korisnicko-ime [{:keys [vrednost on-change tekst-greske ispravno?]}]
  (let [[css] (styles/use-styletron)]
    [:div.form-group.mb-3
     [:label {:className (css {:color "#757575" :font-size "25px"})} "Korisničko ime:"]
     [:input {:className (css (:input-field styles/styles-map))
              :value vrednost
              :on-change on-change
              :type "text"
              :placeholder "Ime koje će se prikazivati pored vaših oglasa"}]
     (when (and (some? vrednost) (false? ispravno?))
       [:p.text-danger {:className (css (:error styles/styles-map))}
        tekst-greske])]))

(defn text-area [{:keys [vrednost on-change tekst-greske ispravno? natpis placeholder]}]
  (let [[css] (styles/use-styletron)]
    [:div.form-group.mb-3
     (when-not (or (nil? natpis) (str/blank? natpis))
       [:label {:className (css (:label styles/styles-map))} natpis])
     [:textarea {:className (css (:input-field styles/styles-map))
                 :value vrednost
                 :on-change on-change
                 :type "text"
                 :placeholder placeholder}]
     (when (false? ispravno?)
       [:p.text-danger {:className (css (:error styles/styles-map))}
        tekst-greske])]))
