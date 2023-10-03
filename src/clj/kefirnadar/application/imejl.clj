(ns kefirnadar.application.imejl
  (:require [postal.core :as email]
            [hiccup.core :as h]
            [kefirnadar.configuration.config :as config]))

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

(defn posalji-imejl
  [to tema sadrzaj]
  (email/send-message {:host (config/smtp-host)
                       :user (config/smtp-korisnik)
                       :pass (config/smtp-lozinka)
                       :port (config/smtp-port)
                       :tls true}
    {:from "toromanj+kefirnadar@gmail.com"
     :to [to]
     :subject tema
     :body [{:type "text/html; charset=utf-8"
             :content sadrzaj}]}))
