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
  (postgres/execute-query! {:select [:ad_id]
                            :from [:ad]
                            :where [:< :ad.created_on [:raw ["NOW() - INTERVAL '30 days'"]]]}))

(defn dohvati-oglase
  [{:keys [page-number page-size regions seeking-milk-type? seeking-water-type? seeking-kombucha? receive-by-post?
           receive-in-person? id-korisnika]}]
  (let [odmik (when (and (some? page-number) (some? page-size))
                (* (dec page-number) page-size))
        where-klauzula (cond-> []
                         (and (seq regions) (some (comp not str/blank?) regions)) (conj [:in :ad.region regions])

                         (some true? [seeking-milk-type? seeking-water-type? seeking-kombucha?])
                         (conj (cond-> [:or]
                                 seeking-milk-type? (conj [:= :ad.sharing_milk_type true])
                                 seeking-water-type? (conj [:= :ad.sharing_water_type true])
                                 seeking-kombucha? (conj [:= :ad.sharing_kombucha true])))

                         (some true? [receive-by-post? receive-in-person?])
                         (conj (cond-> [:or]
                                 receive-by-post? (conj [:= :ad.send_by_post true])
                                 receive-in-person? (conj [:= :ad.share_in_person true])))

                         (some? id-korisnika) (conj [:= :ad.id_korisnika id-korisnika]))]
    (log/debug "Where klauzula: " (with-out-str (pprint/pprint where-klauzula)))
    (log/spy :debug
      {:ads (postgres/execute-query!
              (cond-> {:select [:created_on :korisnicko_ime :region :send_by_post :share_in_person
                                :quantity :broj_telefona :imejl :ad_id :sharing_milk_type :sharing_water_type
                                :sharing_kombucha :korisnik.id_korisnika]
                       :from [:ad]
                       :left-join [:korisnik [:= :ad.id_korisnika :korisnik.id_korisnika]]
                       :order-by [[:ad.created_on :desc]]}
                (some? page-size) (assoc :limit page-size)
                (some? odmik) (assoc :offset odmik)
                (seq where-klauzula) (assoc :where (into [:and] where-klauzula))))
       :ads-count (:count (postgres/execute-one!
                            (cond-> {:select [:%count.*]
                                     :from [:ad]}
                              (seq where-klauzula) (assoc :where (into [:and] where-klauzula)))))})))

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
(defn dodaj-oglas
  [oglas]
  (log/debug "dodaj-oglas: " oglas)
  (postgres/execute-transaction! {:insert-into [:ad]
                                  :columns (vec (keys oglas))
                                  :values [(vec (vals oglas))]}))

(defn izbrisi-oglas
  [id-oglasa]
  (log/debug "izbrisi-oglas: " id-oglasa)
  (postgres/execute-transaction! {:delete-from [:ad]
                                  :where [:= :ad.ad_id id-oglasa]}))

(defn izbrisi-oglase-korisnika
  [id-korisnika]
  (log/debug "izbrisi-oglase-korisnika: " id-korisnika)
  (postgres/execute-transaction! {:delete-from [:ad]
                                  :where [:= :ad.id_korisnika id-korisnika]}))

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
     :columns [:email :hes_lozinke :korisnicko_ime :aktivacioni_kod :aktiviran]
     :values [[imejl (password/encrypt lozinka) korisnicko-ime aktivacioni-kod false]]})
  (dohvati-korisnika-po-imejlu imejl))

(defn aktiviraj-korisnika
  [aktivacioni-kod]
  (postgres/execute-transaction! {:update [:korisnik]
                                  :set {:aktivacioni_kod "" :aktiviran true}
                                  :where [:= :aktivacioni_kod aktivacioni-kod]}))

(defn resetuj-lozinku
  [kod-za-resetovanje-lozinke nova-lozinka]
  (postgres/execute-transaction! {:update [:korisnik]
                                  :set {:kod_za_resetovanje_lozinke "" :hes_lozinke (password/encrypt nova-lozinka)}
                                  :where [:= :kod_za_resetovanje_lozinke kod-za-resetovanje-lozinke]}))
;; endregion


(comment
  ;; add user
  (postgres/execute-transaction! {:insert-into [:korisnik]
                                  :columns [:facebook_user_id :ime :prezime]
                                  :values [["testuserid" "ime" "prezime"]]})

  (postgres/execute-transaction! {:delete-from [:korisnik]
                                  :where [:= :korisnik.id_korisnika 20]})
  ;; get user
  (dohvati-facebook-korisnika "testuserid")

  ;; get info for a specific ad
  (postgres/execute-query!
    {:select [:created_on :ime :prezime :region :send_by_post :share_in_person
              :quantity :phone_number :email :ad_id :sharing_milk_type :sharing_water_type
              :sharing_kombucha :korisnik.id_korisnika]
     :from [:ad]
     :join [:korisnik [:= :ad.id_korisnika :korisnik.id_korisnika]]
     :where [:= :ad.ad_id 2]})

  (postgres/execute-query! {:select [:*]
                            :from :korisnik}))

(comment "Remove all ads"
  (postgres/execute-transaction! {:truncate [:ad]}))
