(ns kefirnadar.application.web
  (:require [kefirnadar.application.routes :as routes]
            [muuntaja.core :as m]
            [reitit.coercion.spec]
            [reitit.coercion.spec]
            [reitit.ring :as ring]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.cors :refer [wrap-cors]]))

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
                           [wrap-cors :access-control-allow-origin #".*"
                            :access-control-allow-methods [:get :put :post :delete]]]}})
    (ring/create-default-handler)))