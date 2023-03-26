(ns kefirnadar.common.coercion)

(defn coerce-regions
  [{:keys [regions] :as params}]
  (if (some? regions)
    (update params :regions #(if (coll? %) % [%]))
    params))
