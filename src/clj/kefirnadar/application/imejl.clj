(ns kefirnadar.application.imejl
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [hiccup2.core :as h]
            [kefirnadar.configuration.config :as config]
            [taoensso.timbre :as log]))

(defn aktivacioni-imejl [url]
  (str (h/html
         [:div
          [:p "Molimo otvorite sledeću vezu kako biste aktivirali svoj nalog na sajtu kefirnadar.rs: "]
          [:br]
          [:a {:href url} "Aktiviraj korisnika"]])))

(defn resetovanje-lozinke-imejl [url]
  (str (h/html
         [:div
          [:p "Molimo otvorite sledeću vezu kako biste resetovali lozinku na sajtu kefirnadar.rs: "]
          [:br]
          [:a {:href url} "Resetuj lozinku"]])))

(defn kontakt-poruka [imejl poruka]
  (str (h/html
         [:div
          [:h1 "Pošiljalac poruke: " imejl]
          [:p "Poruka: " poruka]])))

(defn posalji-imejl
  [imejl-primaoca tema sadrzaj & {:keys [reply-to]}]
  (log/debugf "Šaljem imejl poruku koristeći MailJet na %s sa temom %s" imejl-primaoca tema)
  (let [{:keys [body]} (http/post "https://api.mailjet.com/v3.1/send"
                         {:basic-auth [@config/api-kljuc @config/api-tajna]
                          :body (json/write-str {:Messages
                                                 [(cond-> {:From {:Email @config/adresa-posiljaoca :Name "Kefir na dar"}
                                                           :To [{:Email imejl-primaoca}]
                                                           :Subject tema
                                                           :TextPart sadrzaj
                                                           :HtmlPart sadrzaj}
                                                    (not (str/blank? reply-to)) (assoc :ReplyTo {:Email reply-to}))]})
                          :throw-exceptions false})
        {[{:keys [Status]}] :Messages :as response} (json/read-str body :key-fn keyword)]
    (log/debug (if (= Status "success")
                 "Poruka poslata uspešno"
                 (str "Poruka nije poslata jer je došlo do greške: " (with-out-str (pprint/pprint response)))))))
