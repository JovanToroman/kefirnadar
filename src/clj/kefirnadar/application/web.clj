(ns kefirnadar.application.web
  (:require [kefirnadar.application.routes :as routes]
            [muuntaja.core :as m]
            [reitit.coercion.spec]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.middleware.cors :refer [wrap-cors]]))

(defn wrap-merge-params
  [handler]
  (fn [request]
    (if-let [params (->> request
                      :parameters
                      vals
                      (apply merge))]
      (handler (assoc request :params params))
      (handler request))))

(def app
  (ring/ring-handler
    (ring/router
      routes/routes
      {:conflicts identity
       :data {:coercion reitit.coercion.spec/coercion
              :muuntaja m/instance
              :middleware [parameters/parameters-middleware
                           muuntaja/format-negotiate-middleware
                           muuntaja/format-response-middleware
                           muuntaja/format-request-middleware
                           coercion/coerce-response-middleware
                           coercion/coerce-request-middleware
                           coercion/coerce-exceptions-middleware
                           wrap-merge-params
                           [wrap-cors :access-control-allow-origin #".*"
                            :access-control-allow-methods [:delete :get
                                                           :patch :post :put]]]}})
    (ring/create-default-handler)))