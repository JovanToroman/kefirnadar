(ns kefirnadar.application.handlers
  (:require [kefirnadar.application.queries :as q]
            [ring.util.http-response :as r]
            [kefirnadar.common.coercion :as coerce-common]
            [taoensso.timbre :as log]
            [cuerdas.core :as str]))

(defn get-ads
  "This handler returns active ads."
  [{params :params}]
  (log/debug "'get-ads' params: " params)
  (let [params (coerce-common/coerce-regions params)]
    (r/ok (q/get-ads params))))

(defn prilagodi-korisnika-za-frontend [korisnik]
  (update-keys korisnik (comp keyword str/kebab)))

(defn create-ad
  "Creates ad."
  [{:keys [parameters]}]
  (let [id-korisnika (get-in parameters [:body :korisnik/id])
        entity-body {:id_korisnika id-korisnika
                     :region (get-in parameters [:body :ad/region])
                     :send_by_post (get-in parameters [:body :ad/post?] false)
                     :share_in_person (get-in parameters [:body :ad/pick-up?] false)
                     :quantity (get-in parameters [:body :ad/quantity])
                     :created_on [:now]                     ;; Postgresql internal function
                     :sharing_milk_type (get-in parameters [:body :ad/sharing-milk-type?])
                     :sharing_water_type (get-in parameters [:body :ad/sharing-water-type?])
                     :sharing_kombucha (get-in parameters [:body :ad/sharing-kombucha?])}
        phone-number (get-in parameters [:body :ad/phone-number])
        email (get-in parameters [:body :ad/email])
        {:keys [next.jdbc/update-count]} (q/add-ad entity-body)]
    (when (not (str/blank? email))
      (q/azuriraj-korisnika id-korisnika :email email))
    (when (not (str/blank? phone-number))
      (q/azuriraj-korisnika id-korisnika :phone_number phone-number))
    (if (= update-count 1)
      (r/ok {:oglas entity-body
             :korisnik (prilagodi-korisnika-za-frontend (q/dohvati-korisnika id-korisnika))})
      (r/bad-request!))))

(defn ensure-user
  [{{params :body} :parameters}]
  (let [korisnik (prilagodi-korisnika-za-frontend (q/dodaj-korisnika params))]
    (r/ok {:korisnik korisnik})))
