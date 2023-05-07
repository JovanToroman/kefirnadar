(ns kefirnadar.application.queries
  (:require
    [clojure.string :as str]
    [kefirnadar.configuration.postgres :as postgres]
    [taoensso.timbre :as log]))

;; region queries
(defn get-expired-ads
  []
  (postgres/execute-transaction! {:select [:ad_id]
                                  :from [:ad]
                                  :where [:< :ad.created_on [:raw ["NOW() - INTERVAL '30 days'"]]]}))

(defn get-ads
  [{:keys [page-number page-size regions seeking-milk-type? seeking-water-type? seeking-kombucha? receive-by-post?
           receive-in-person?]}]
  (let [offset (* (dec page-number) page-size)
        where-clause (cond-> []
                       (seq regions) (conj [:in :ad.region regions])
                       (some boolean? [seeking-milk-type? seeking-water-type? seeking-kombucha?])
                       (conj (cond-> [:or]
                               (some? seeking-milk-type?) (conj [:= :ad.sharing_milk_type seeking-milk-type?])
                               (some? seeking-water-type?) (conj [:= :ad.sharing_water_type seeking-water-type?])
                               (some? seeking-kombucha?) (conj [:= :ad.sharing_kombucha seeking-kombucha?])))
                       (some boolean? [receive-by-post? receive-in-person?])
                       (conj (cond-> [:or]
                               (some? receive-by-post?) (conj [:= :ad.send_by_post receive-by-post?])
                               (some? receive-in-person?) (conj [:= :ad.share_in_person receive-in-person?]))))]
    (log/debug "Where clause: " where-clause)
    (log/spy :debug
      {:ads (postgres/execute-query!
              (cond-> {:select [:created_on :ime :prezime :region :send_by_post :share_in_person
                                :quantity :phone_number :email :ad_id :sharing_milk_type :sharing_water_type
                                :sharing_kombucha :korisnik.id_korisnika]
                       :from [:ad]
                       :join [:korisnik [:= :ad.id_korisnika :korisnik.id_korisnika]]
                       :limit page-size
                       :offset offset
                       :order-by [[:ad.created_on :desc]]}
                (seq where-clause) (assoc :where (into [:and] where-clause))))
       :ads-count (:count (postgres/execute-one!
                            (cond-> {:select [:%count.*]
                                     :from [:ad]}
                              (seq where-clause) (assoc :where (into [:and] where-clause)))))})))
;; endregion

;; region transactions
(defn add-ad
  [ad-info]
  (log/info :debug "add-ad: " ad-info)
  (postgres/execute-transaction! {:insert-into [:ad]
                                  :columns (vec (keys ad-info))
                                  :values [(vec (vals ad-info))]}))

(defn remove-old-ads
  [ad-ids]
  (log/info :debug "remove-old-ads: " ad-ids)
  (postgres/execute-transaction! {:delete-from [:ad]
                                  :where [:in :ad.ad_id ad-ids]}))

(defn dohvati-korisnika
  [id-korisnika]
  (postgres/execute-one!
    {:select [:id_korisnika :facebook_user_id :ime :prezime :phone-number :email]
     :from [:korisnik]
     :where [:= :korisnik.id_korisnika id-korisnika]}))

(defn dohvati-facebook-korisnika
  [user-id]
  (postgres/execute-one!
    {:select [:id_korisnika :facebook_user_id :ime :prezime :phone-number :email]
     :from [:korisnik]
     :where [:= :korisnik.facebook_user_id user-id]}))

(defn dodaj-korisnika [{:keys [_accessToken _expiresIn _signedRequest userID name]}]
  (let [korisnik (dohvati-facebook-korisnika userID)]
    (when (nil? korisnik)
      (log/info :debug "Dodajem korisnika: " userID)
      (let [[ime prezime] (str/split name #" " 2)]
        (postgres/execute-transaction! {:insert-into [:korisnik]
                                        :columns [:facebook_user_id :ime :prezime]
                                        :values [[userID ime prezime]]})))
    (dohvati-facebook-korisnika userID)))

(defn azuriraj-korisnika [id-korisnika kolona vrednost]
  (postgres/execute-transaction! {:update :korisnik
                                  :set {kolona vrednost}
                                  :where [:= :id_korisnika id-korisnika]}))
;; endregion


(comment
  ;; add user
  (postgres/execute-transaction! {:insert-into [:korisnik]
                                  :columns [:facebook_user_id :ime :prezime]
                                  :values [["testuserid" "ime" "prezime"]]})

  ;; get user
  (dohvati-facebook-korisnika "testuserid")

  (mapv (fn [no]
          (add-ad {:send_by_post true,
                   :email "",
                   :first_name "asdasd",
                   :phone_number (str "062" no),
                   :region "Apatin",
                   :created_on [:now],
                   :last_name "asdassadsad",
                   ;;:user_id "10226481938492906",
                   :quantity 3,
                   :share_in_person false
                   :sharing_milk_type true}))
    (range 100000 100010)))

(comment "Remove all ads"
  (postgres/execute-transaction! {:truncate [:ad]}))
