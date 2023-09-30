(ns kefirnadar.application.db
  (:require
    [clojure.pprint :as pprint]
    [clojure.string :as str]
    [kefirnadar.configuration.postgres :as postgres]
    [crypto.password.pbkdf2 :as password]
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
                       (some true? [seeking-milk-type? seeking-water-type? seeking-kombucha?])
                       (conj (cond-> [:or]
                               seeking-milk-type? (conj [:= :ad.sharing_milk_type true])
                               seeking-water-type? (conj [:= :ad.sharing_water_type true])
                               seeking-kombucha? (conj [:= :ad.sharing_kombucha true])))
                       (some true? [receive-by-post? receive-in-person?])
                       (conj (cond-> [:or]
                               receive-by-post? (conj [:= :ad.send_by_post true])
                               receive-in-person? (conj [:= :ad.share_in_person true]))))]
    (log/debug "Where clause: " (with-out-str (pprint/pprint where-clause)))
    (log/spy :debug
      {:ads (postgres/execute-query!
              (cond-> {:select [:created_on :korisnicko_ime :region :send_by_post :share_in_person
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

(defn dohvati-korisnika
  [id-korisnika]
  (postgres/execute-one!
    {:select [:id_korisnika :facebook_user_id :ime :prezime :phone-number :email]
     :from [:korisnik]
     :where [:= :korisnik.id_korisnika id-korisnika]}))

(defn- dohvati-facebook-korisnika
  [user-id]
  (postgres/execute-one!
    {:select [:id_korisnika :facebook_user_id :ime :prezime :phone-number :email]
     :from [:korisnik]
     :where [:= :korisnik.facebook_user_id user-id]}))

(def kolone-korisnika [:id_korisnika :ime :prezime :phone-number :email :korisnicko_ime :hes_lozinke :aktiviran])

(defn dohvati-korisnika-po-imejlu
  [imejl]
  (postgres/execute-one!
    {:select kolone-korisnika
     :from [:korisnik]
     :where [:= :korisnik.email imejl]}))

(defn dohvati-korisnika-po-korisnickom-imenu
  [korisnicko-ime]
  (postgres/execute-one!
    {:select kolone-korisnika
     :from [:korisnik]
     :where [:= :korisnik.korisnicko_ime korisnicko-ime]}))

(defn dohvati-korisnika-po-aktivacionom-kodu
  [aktivacioni-kod]
  (postgres/execute-one!
    {:select kolone-korisnika
     :from [:korisnik]
     :where [:= :korisnik.aktivacioni_kod aktivacioni-kod]}))

(defn dohvati-korisnika-po-kodu-za-resetovanje-lozinke
  [kod-za-resetovanje-lozinke]
  (postgres/execute-one!
    {:select kolone-korisnika
     :from [:korisnik]
     :where [:= :korisnik.kod_za_resetovanje_lozinke kod-za-resetovanje-lozinke]}))
;; endregion

;; region transactions
(defn add-ad
  [ad-info]
  (log/debug "add-ad: " ad-info)
  (postgres/execute-transaction! {:insert-into [:ad]
                                  :columns (vec (keys ad-info))
                                  :values [(vec (vals ad-info))]}))

(defn remove-old-ads
  [ad-ids]
  (log/debug "remove-old-ads: " ad-ids)
  (postgres/execute-transaction! {:delete-from [:ad]
                                  :where [:in :ad.ad_id ad-ids]}))

(defn dodaj-facebook-korisnika [{:keys [_accessToken _expiresIn _signedRequest userID name]}]
  (let [korisnik (dohvati-facebook-korisnika userID)]
    (when (nil? korisnik)
      (log/debug "Dodajem korisnika: " userID)
      (let [[ime prezime] (str/split name #" " 2)]
        (postgres/execute-transaction! {:insert-into [:korisnik]
                                        :columns [:facebook_user_id :ime :prezime]
                                        :values [[userID ime prezime]]})))
    (dohvati-facebook-korisnika userID)))

(defn azuriraj-korisnika [[id-kolona id-vrednost] kolona vrednost]
  (postgres/execute-transaction! {:update :korisnik
                                  :set {kolona vrednost}
                                  :where [:= id-kolona id-vrednost]}))

(defn dodaj-korisnika [{:keys [imejl lozinka korisnicko-ime aktivacioni-kod]}]
  (log/debug "Dodajem korisnika: " imejl)
  (postgres/execute-transaction!
    {:insert-into [:korisnik]
     :columns     [:email :hes_lozinke :korisnicko_ime :aktivacioni_kod :aktiviran]
     :values      [[imejl (password/encrypt lozinka) korisnicko-ime aktivacioni-kod false]]})
  (dohvati-korisnika-po-imejlu imejl))

(defn aktiviraj-korisnika
  [aktivacioni-kod]
  (postgres/execute-transaction! {:update [:korisnik]
                                  :set    {:aktivacioni_kod "" :aktiviran true}
                                  :where  [:= :aktivacioni_kod aktivacioni-kod]}))

(defn resetuj-lozinku
  [kod-za-resetovanje-lozinke nova-lozinka]
  (postgres/execute-transaction! {:update [:korisnik]
                                  :set    {:kod_za_resetovanje_lozinke "" :hes_lozinke (password/encrypt nova-lozinka)}
                                  :where  [:= :kod_za_resetovanje_lozinke kod-za-resetovanje-lozinke]}))
;; endregion


(comment
  ;; add user
  (postgres/execute-transaction! {:insert-into [:korisnik]
                                  :columns [:facebook_user_id :ime :prezime]
                                  :values [["testuserid" "ime" "prezime"]]})

  ;; get user
  (dohvati-facebook-korisnika "testuserid")

  (mapv (fn [no]
          (add-ad {:send_by_post false,
                   :region "Ada",
                   :created_on [:now],
                   :id_korisnika 1
                   :quantity 22,
                   :share_in_person true
                   :sharing_water_type true}))
    (range 100000 100010))

  ;; get info for a specific ad
  (postgres/execute-query!
    {:select [:created_on :ime :prezime :region :send_by_post :share_in_person
              :quantity :phone_number :email :ad_id :sharing_milk_type :sharing_water_type
              :sharing_kombucha :korisnik.id_korisnika]
     :from [:ad]
     :join [:korisnik [:= :ad.id_korisnika :korisnik.id_korisnika]]
     :where [:= :ad.ad_id 2]}))

(comment "Remove all ads"
  (postgres/execute-transaction! {:truncate [:ad]}))
