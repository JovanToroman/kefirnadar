(ns kefirnadar.application.imejl
  (:require [postal.core :as email]
            [hiccup.core :as h]
            [kefirnadar.configuration.config :as config]
            [taoensso.timbre :as log]))

(defn aktivacioni-imejl [url]
  (h/html
    [:div
     [:p "Molimo otvorite sledeću vezu kako biste aktivirali svoj nalog na sajtu kefirnadar.rs: "]
     [:br]
     [:a {:href url} "Aktiviraj korisnika"]]))

(defn resetovanje-lozinke-imejl [url]
  (h/html
    [:div
     [:p "Molimo otvorite sledeću vezu kako biste resetovali lozinku na sajtu kefirnadar.rs: "]
     [:br]
     [:a {:href url} "Resetuj lozinku"]]))

(defn kontakt-poruka [imejl poruka]
  (h/html
    [:div
     [:h1 "Pošiljalac poruke: " imejl]
     [:p "Poruka: " poruka]]))

(defn posalji-imejl
  [to tema sadrzaj]
  (log/debugf "Šaljem imejl poruku na %s sa temom %s" to tema)
  (let [{:keys [code]} (email/send-message {:host @config/smtp-host
                                            :user @config/smtp-korisnik
                                            :pass @config/smtp-lozinka
                                            :port @config/smtp-port
                                            :tls true}
                         {:from "kefirnadar@gmail.com"
                          :to [to]
                          :subject tema
                          :body [{:type "text/html; charset=utf-8"
                                  :content sadrzaj}]})]
    (log/debug (if (= code 0)
                 "Poruka poslata uspešno"
                 "Poruka nije poslata jer je došlo do greške"))))
