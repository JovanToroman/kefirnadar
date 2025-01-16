(ns kefirnadar.application.handlers
  (:require [crypto.password.pbkdf2 :as password]
            [kefirnadar.application.db :as db]
            [ring.util.http-response :as r]
            [kefirnadar.common.coercion :as coerce-common]
            [taoensso.timbre :as log]
            [cuerdas.core :as str]
            [kefirnadar.application.imejl :as imejl]))

(defn dohvati-oglase
  [{params :params}]
  (log/debug "'get-ads' params: " params)
  (let [params (coerce-common/coerce-regions params)]
    (r/ok (db/dohvati-oglase params))))

(defn prilagodi-korisnika-za-frontend [korisnik]
  (update-keys korisnik (comp keyword str/kebab)))

(defn postavi-oglas
  [{:keys [parameters]}]
  (let [id-korisnika (get-in parameters [:body :korisnik/id])
        broj-telefona (get-in parameters [:body :ad/phone-number])
        imejl (get-in parameters [:body :ad/email])
        entity-body (cond-> {:id_korisnika id-korisnika
                             :region (get-in parameters [:body :ad/region])
                             :send_by_post (get-in parameters [:body :ad/post?] false)
                             :share_in_person (get-in parameters [:body :ad/pick-up?] false)
                             :quantity (get-in parameters [:body :ad/quantity])
                             :created_on [:now]             ;; Postgresql internal function
                             :sharing_milk_type (get-in parameters [:body :ad/sharing-milk-type?])
                             :sharing_water_type (get-in parameters [:body :ad/sharing-water-type?])
                             :sharing_kombucha (get-in parameters [:body :ad/sharing-kombucha?])}
                      (some? broj-telefona) (assoc :broj_telefona broj-telefona)
                      (some? imejl) (assoc :imejl imejl))
        {:keys [next.jdbc/update-count]} (db/dodaj-oglas entity-body)]
    ;;(when (not (str/blank? email))
    ;;  (db/azuriraj-korisnika [:id_korisnika id-korisnika] :email email))
    ;;(when (not (str/blank? phone-number))
    ;;  (db/azuriraj-korisnika [:id_korisnika id-korisnika] :phone_number phone-number))
    (if (= update-count 1)
      (r/ok {:oglas entity-body
             :korisnik (prilagodi-korisnika-za-frontend (db/dohvati-korisnika id-korisnika))})
      (r/bad-request!))))

(defn potvrdi-fejsbuk-korisnika
  "Dodaje korisnika ako ne postoji u bazi"
  [{{params :body} :parameters}]
  (let [korisnik (prilagodi-korisnika-za-frontend (db/dodaj-facebook-korisnika params))]
    (r/ok {:korisnik korisnik})))

(defn dodaj-korisnika
  [{{{:keys [imejl korisnicko-ime] :as params} :body} :parameters {origin "origin"} :headers}]
  (let [korisnik-postoji? (some? (db/dohvati-korisnika-po-imejlu imejl))
        korisnicko-ime-zauzeto? (some? (db/dohvati-korisnika-po-korisnickom-imenu korisnicko-ime))]
    (cond
      korisnik-postoji? (r/bad-request {:greska :imejl-vec-iskoriscen})
      korisnicko-ime-zauzeto? (r/bad-request {:greska :korisnicko-ime-zauzeto})
      :else (let [aktivacioni-kod (str (random-uuid))]
              (imejl/posalji-imejl imejl "Aktivacija naloga u aplikaciji Kefirnadar"
                (imejl/aktivacioni-imejl (format "%s/aktiviraj-korisnika/%s" origin aktivacioni-kod)))
              (db/dodaj-korisnika (assoc params :aktivacioni-kod aktivacioni-kod))
              (r/ok)))))

(defn posalji-aktivacioni-kod
  [{{{:keys [imejl]} :body} :parameters {origin "origin"} :headers}]
  (let [aktivacioni-kod (str (random-uuid))]
    (imejl/posalji-imejl imejl "Aktivacija naloga u aplikaciji Kefirnadar"
      (imejl/aktivacioni-imejl (format "%s/aktiviraj-korisnika/%s" origin aktivacioni-kod)))
    (db/azuriraj-korisnika [:email imejl] :aktivacioni_kod aktivacioni-kod)
    (r/ok)))


(defn aktiviraj-korisnika
  [{{{:keys [aktivacioni-kod]} :body} :parameters}]
  (let [{:korisnik/keys [email]} (db/dohvati-korisnika-po-aktivacionom-kodu aktivacioni-kod)
        aktivacioni-kod-ispravan? (some? email)]
    (log/debugf "aktiviraj-korisnika pozvan sa imejl adresom %s i aktivacionim kodom %s" email aktivacioni-kod)
    (if (and (some? aktivacioni-kod) aktivacioni-kod-ispravan?)
      (do (db/aktiviraj-korisnika aktivacioni-kod)
          (r/ok))
      (r/bad-request {:greska :aktivacioni-kod-neispravan}))))

(defn posalji-imejl-za-resetovanje-lozinke
  [{{{:keys [imejl]} :body} :parameters {origin "origin"} :headers}]
  (if (some? (db/dohvati-korisnika-po-imejlu imejl))
    (let [kod-za-resetovanje-lozinke (str (random-uuid))]
      (imejl/posalji-imejl imejl "Resetovanje lozinke u aplikaciji Kefirnadar"
        (imejl/resetovanje-lozinke-imejl (format "%s/resetovanje-lozinke/%s" origin kod-za-resetovanje-lozinke)))
      (db/azuriraj-korisnika [:email imejl] :kod_za_resetovanje_lozinke kod-za-resetovanje-lozinke)
      (r/ok))
    (r/bad-request)))
;; podesiti gresku za ovo gore

(defn resetuj-lozinku
  [{{{:keys [kod-za-resetovanje-lozinke nova-lozinka]} :body} :parameters}]
  (let [{:korisnik/keys [email]} (db/dohvati-korisnika-po-kodu-za-resetovanje-lozinke
                                   kod-za-resetovanje-lozinke)
        kod-za-resetovanje-lozinke-ispravan? (some? email)]
    (log/debugf "resetuj-lozinku pozvan za imejl adresu %s i kodom za resetovanje lozinke %s"
      email kod-za-resetovanje-lozinke)
    (cond
      (not kod-za-resetovanje-lozinke-ispravan?) (r/bad-request {:greska :kod-za-resetovanje-lozinke-neispravan})
      :else (do (db/resetuj-lozinku kod-za-resetovanje-lozinke nova-lozinka)
                (r/ok)))))

(defn prijava
  [{{{:keys [imejl lozinka]} :body} :parameters}]
  (if-some [{:korisnik/keys [hes_lozinke aktiviran] :as korisnik} (db/dohvati-korisnika-po-imejlu imejl)]
    (cond
      (not aktiviran) (r/bad-request {:greska :korisnik-nije-aktiviran})
      (not (password/check lozinka hes_lozinke)) (r/bad-request {:greska :pogresna-lozinka})
      :else (r/ok {:korisnik (prilagodi-korisnika-za-frontend korisnik)}))
    (r/bad-request {:greska :korisnik-ne-postoji})))

(defn posalji-kontakt-poruku
  [{{{:keys [imejl poruka]} :body} :parameters}]
  (imejl/posalji-imejl "info@kefirnadar.rs" "Poruka od korisnika"
    (imejl/kontakt-poruka imejl poruka))
  (r/ok))
