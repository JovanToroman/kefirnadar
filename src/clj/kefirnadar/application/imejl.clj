(ns kefirnadar.application.imejl
  (:require [clojure.data.json :as json]
            [hiccup.core :as h]
            [kefirnadar.configuration.config :as config]
            [clj-http.client :as http]
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
  [imejl-primaoca tema sadrzaj]
  (log/debugf "Šaljem imejl poruku koristeći MailJet na %s sa temom %s" imejl-primaoca tema)
  (let [{:keys [body]} (http/post "https://api.mailjet.com/v3.1/send"
                                  {:basic-auth [@config/api-kljuc @config/api-tajna]
                                   :body (json/write-str {:Messages
                                                          [{:From {:Email @config/adresa-posiljaoca :Name "Kefir na dar"}
                                                            :To [{:Email imejl-primaoca}]
                                                            :Subject tema
                                                            :TextPart sadrzaj
                                                            :HtmlPart sadrzaj}]})
                                   :throw-exceptions false})
        {[{:keys [Status]}] :Messages} (json/read-str body :key-fn keyword)]
    (log/debug (if (= Status "success")
                 "Poruka poslata uspešno"
                 "Poruka nije poslata jer je došlo do greške"))))
