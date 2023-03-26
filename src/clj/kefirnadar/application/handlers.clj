(ns kefirnadar.application.handlers
  (:require [kefirnadar.application.queries :as q]
            [ring.util.http-response :as r]
            [kefirnadar.common.coercion :as coerce-common]
            [taoensso.timbre :as log]))

(defn get-ads
  "This handler returns active ads."
  [{params :params}]
  (log/debug "'get-ads' params: " params)
  (let [params (coerce-common/coerce-regions params)]
    (r/ok (q/get-ads params))))

(defn create-ad
  "Creates ad."
  [{:keys [parameters]}]
  (let [entity-body {:first_name (get-in parameters [:body :ad/firstname])
                     :last_name (get-in parameters [:body :ad/lastname])
                     :region (get-in parameters [:body :ad/region])
                     :send_by_post (get-in parameters [:body :ad/post?] false)
                     :share_in_person (get-in parameters [:body :ad/pick-up?] false)
                     :quantity (get-in parameters [:body :ad/quantity])
                     :created_on [:now]                     ;; Postgresql internal function
                     :sharing_milk_type (get-in parameters [:body :ad/sharing-milk-type?])
                     :sharing_water_type (get-in parameters [:body :ad/sharing-water-type?])
                     :sharing_kombucha (get-in parameters [:body :ad/sharing-kombucha?])
                     #_#_:user_id (get-in parameters [:body :user-id])}
        phone-number (get-in parameters [:body :ad/phone-number])
        email (get-in parameters [:body :ad/email])
        assembled-entity-body (cond
                                (and phone-number email) (assoc entity-body :phone_number phone-number :email email)
                                phone-number (assoc entity-body :phone_number phone-number)
                                email (assoc entity-body :email email))
        {:keys [next.jdbc/update-count]} (q/add-ad assembled-entity-body)]
    (if (= update-count 1)
      (r/ok assembled-entity-body)
      (r/bad-request!))))

