(ns kefirnadar.application.inputs
  "A place for all UI components which are generic and reusable"
  (:require [reagent.core :as r]
            [kefirnadar.application.utils :as utils]
            [applied-science.js-interop :as j]
            [kefirnadar.application.styles :as styles]))

(defn- render-options
  [filtered-options active-value]
  (doall
    (for [{:keys [value title on-click]} filtered-options
          :let [is-active? (= active-value value)]]
      ^{:key value}
      [:li
       [:button {:class (when is-active? "active")
                                               :title title
                                               :on-click on-click}
        title]])))

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
               placeholder-disabled? false}
          :as params}]
      (js/console.log "Params to search: " params)
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
        [:div
         [:button
          (cond-> {:id id
                   :aria-expanded (str @show-options?)
                   :aria-haspopup "true"
                   :type "button"
                   :on-click (fn [_] (swap! show-options? not))
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
            [:span placeholder])

          [:span {:className (css {    :display "flex"
                                   :font-size "28px"})}
           [:i.fa.fa-angle-down {:aria-hidden "true"}]]]

         (when @show-options?
           [:div
            [:div
             [:input.dd-select__input.form-control
              {:placeholder "Unesite vrednost pomoću tastature"
               :type "text"
               :on-change (fn [event] (reset! search-text (j/get-in event [:target :value])))
               :value @search-text
               :aria-label "Search"}]
             [:i.fal.fa-keyboard {:aria-hidden "true"}]]
            [:ul {:style {:list-style-type "none" :padding 0 :margin 0}}
             [:li placeholder]
             ;; TODO: namestiti on-click funkciju za dropdown iteme
             (render-options filtered-options active-value)]])]))))
