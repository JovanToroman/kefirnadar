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