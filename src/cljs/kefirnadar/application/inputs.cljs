(ns kefirnadar.application.inputs
  "A place for all UI components which are generic and reusable"
  (:require [reagent.core :as r]
            [kefirnadar.application.utils.transformations :as transform]
            [applied-science.js-interop :as j]
            [kefirnadar.application.styles :as styles]
            [clojure.string :as str]))

;; TODO: move this to a common ns
(defn extract-input-value
  [event]
  (j/get-in event [:target :value]))

(defn extract-checkbox-state
  [event]
  (j/get-in event [:target :checked]))

(defn- render-options
  [filtered-options active-value ^atom show-options?]
  (doall
    (for [{:keys [value title on-click]} filtered-options
          :let [is-active? (= active-value value)]]
      ^{:key value}
      [:button.dropdown-item {:class (when is-active? "active")
                              :title title
                              :value (keyword value)
                              :on-click (fn [event]
                                          (on-click event)
                                          (swap! show-options? not))
                              :type "button"}
       title])))

(defn search-selector-dropdown
  [{:keys [search-input-id]}]
  (r/create-class
    {:component-did-mount (fn [_] (j/call (j/call js/document :getElementById search-input-id) :focus))
     :reagent-render
     (fn [{:keys [search-input-id search-text id css filtered-options active-value show-options?]}]
       [:<>
        [:div
         [:input.col-md-12.form-control
          {:id search-input-id
           :placeholder "Unesite vrednost pomoću tastature"
           :type "text"
           :on-change (fn [event] (reset! search-text (j/get-in event [:target :value])))
           :value @search-text
           :aria-label "Search"}]
         [:i.fa.fa-keyboard {:aria-hidden "true"}]]
        [:div.dropdown-menu {:aria-labelledby id :className (css {:display "block"
                                                                  :height :300pt
                                                                  :overflow-y :scroll})}
         (if (seq filtered-options)
           (render-options filtered-options active-value show-options?)
           [:p "Nema rezultata"])]])}))

(defn search-selector [_]
  (let [search-text (r/atom "")
        show-options? (r/atom false)]
    (fn [{:keys [options
                 active-value
                 placeholder
                 aria-labelledby
                 id
                 placeholder-disabled?]
          :or {id "dropdownMenuButton1"
               placeholder-disabled? false}}]
      (let [[css] (styles/use-styletron)
            filtered-options (if (str/blank? @search-text)
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
                                    :font-size "14px"
                                    :line-height "normal"})}
            (seq aria-labelledby) (assoc :aria-labelledby aria-labelledby))

          (if title
            title
            [:span placeholder])]

         (when @show-options?
           [search-selector-dropdown {:search-input-id search-input-id
                                      :search-text search-text
                                      :id id
                                      :css css
                                      :filtered-options filtered-options
                                      :active-value active-value
                                      :show-options? show-options?}])]))))

(defn checkbox [label value on-change]
  (let [[css] (styles/use-styletron)]
    [:div.form-group {:className (css (:input-wrapper styles/styles-map))}
     [:label {:className (css (:label styles/styles-map))} label]
     [:input {:className (css (:input-field styles/styles-map))
              :on-change on-change
              :type "checkbox"
              :checked value}]]))
