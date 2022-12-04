(ns kefirnadar.application.inputs
  "A place for all UI components which are generic and reusable"
  (:require [reagent.core :as r]
            [kefirnadar.application.utils :as utils]
            [applied-science.js-interop :as j]
            [kefirnadar.application.styles :as styles]))

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
            filtered-options (if (empty? @search-text)
                               options
                               (filter (fn [{:keys [title]}]
                                         (re-find
                                           (re-pattern
                                             (str "(?i)" (utils/remove-reserved-characters @search-text)))
                                           title))
                                 options))
            {:keys [title]
             :as _active-option} (some (fn [{:keys [value] :as option}]
                                         (when (= value active-value)
                                           option))
                                   options)]
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
           [:<>
            [:div
             [:input.col-md-12
              {:placeholder "Unesite vrednost pomoću tastature"
               :type "text"
               :on-change (fn [event] (reset! search-text (j/get-in event [:target :value])))
               :value @search-text
               :aria-label "Search"}]
             [:i.fa.fa-keyboard {:aria-hidden "true"}]]
            [:div.dropdown-menu {:aria-labelledby id :className (css {:display "block"})}
             [:button.dropdown-item {:type "button"} placeholder]
             (render-options filtered-options active-value show-options?)]])]))))
