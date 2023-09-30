(ns kefirnadar.application.ui-components
  (:require [kefirnadar.application.styles :as styles]))

(defn spinner [^integer? size]
  (let [[css] (styles/use-styletron)
        size-rem (str size "rem")]
    [:div.spinner-grow {:className (css {:width size-rem :height size-rem}) :role "status"}
     [:span.sr-only "Loading..."]]))
