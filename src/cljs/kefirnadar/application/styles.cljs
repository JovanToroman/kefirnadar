(ns kefirnadar.application.styles
  (:require ["styletron-react" :as styletron-react]
            [reagent.core :as reagent]
            ["styletron-engine-atomic" :refer [Client]]
            ["styletron-react" :as styletron-react]))

(def functional-compiler (reagent/create-compiler {:function-components true}))
(def engine (Client. #js {:prefix "_"}))


(defn provider [{:keys [main-view]}]
  (reagent/create-element styletron-react/Provider #js {:value engine} (reagent/as-element [main-view] functional-compiler)))

(defn use-styletron
  []
  (let [[css] (styletron-react/useStyletron)]
    [(comp css clj->js)]))

(def styles-map {:wrapper       {:max-width "90%"
                                 :width      "fit-content"
                                 :background "#fff"
                                 :margin     "20px auto"}
                 :form-wrapper   {:max-width "100%"
                                 :width      "70%"
                                 :background "#fff"
                                 :margin     "20px auto"
                                 :box-shadow "1px 1px 2px rgba(0,0,0,0.125)"
                                 :padding    "30px"}
                 :wrapper-title {:font-size      "24px"
                                 :font-weight    "700"
                                 :margin-bottom  "25px"
                                 :text-transform "uppercase"
                                 :text-align     "center"}
                 :input-wrapper {:display       "flex"
                                 :margin-bottom "15px"
                                 :align-items   "center"}
                 :input-field   {:width         "100%"
                                 :outline       "none"
                                 :border        "1px solid #d5dbd9"
                                 :font-size     "15px"
                                 :padding       "8px 10px"
                                 :border-radius "3px"
                                 :transition    "all 0.3s ease"
                                 ":focus"       {:border "1px solid blue"}}
                 :label         {:width        "200px"
                                 :color        "#757575"
                                 :margin-right "10px"
                                 :font-size    "16px"}
                 :custom-select {:position "relative"
                                 :width    "100%"
                                 :height   "37px"
                                 ":before" {:content        ""
                                            :position       "absolute"
                                            :top            "12px"
                                            :right          "10px"
                                            :border         "8px solid #d5dbd9 transparent transparent transparent"
                                            :pointer-events "none"}}
                 :select        {:-webkit-appearance "none"
                                 :-moz-appearance    "none"
                                 :appearance         "none"
                                 :outline            "none"
                                 :width              "100%"
                                 :height             "100%"
                                 :border             "1px solid #d5dbd9"
                                 :border-radius      "3px"
                                 :padding            "8px 10px"
                                 :font-size          "15px"
                                 ":focus"            {:border "1px solid blue"}}
                 :p             {:font-size "14px"
                                 :color     "#757575"}
                 :btn           {:width         "100%"
                                 :padding       "8px 10px"
                                 :font-size     "15px"
                                 :border        "1px"
                                 :cursor        "pointer"
                                 :border-radius "3px"
                                 :outline       "none"
                                 :color         "#000fff"
                                 ":hover"       {:background "blue"
                                                 :opacity    "65%"
                                                 :color      "white"}}
                 :error         {:width      "100%"
                                 :outline    "none"
                                 :font-size  "15px"
                                 :padding    "8px 10px"
                                 :transition "all 0.3s ease"}
                 :main-panel {:min-height "80vh"}})