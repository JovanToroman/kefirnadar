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

(defn poziv-za-neaktiviranog-korisnika [veza-za-aktivaciju]
  (str (h/html
         [:html {:lang "sr"}
          [:head
           [:meta {:charset "UTF-8"}]
           [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
           [:title "Aktiviraj svoj nalog i pridruži se Kefir zajednici! 🎉"]]
          [:body {:style "font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; color: #333333;"}
           [:table {:border "0" :cellpadding "0" :cellspacing "0" :width "100%"}
            [:tr
             [:td {:align "center" :style "padding: 20px 0 30px 0;"}
              [:table {:border "0" :cellpadding "0" :cellspacing "0" :width "600" :style "border-collapse: collapse; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);"}
               [:tr
                [:td {:align "center" :style "padding: 40px 0 30px 0; background-color: #007bff; border-radius: 12px 12px 0 0;"}
                 [:h1 {:style "color: #ffffff; margin: 0; font-size: 30px; font-weight: bold; letter-spacing: 2px;"} "Kefir na dar Srbija 🥛"]]]
               [:tr
                [:td {:style "padding: 40px 30px 40px 30px;"}
                 [:table {:border "0" :cellpadding "0" :cellspacing "0" :width "100%"}
                  [:tr
                   [:td {:style "color: #1a1a1a; font-size: 24px; font-weight: bold; padding-bottom: 20px;"} "Dobrodošli, budući kefirdžijo! 👋"]]
                  [:tr
                   [:td {:style "color: #333333; font-size: 16px; line-height: 1.6;"} "Primetili smo da ste se registrovali na našoj super aplikaciji za razmenu **kefirnih zrnaca**, ali tvoj nalog čeka na aktivaciju! Ne propusti priliku da se pridružiš najvećoj zajednici ljubitelja kefira u Srbiji."]]
                  [:tr
                   [:td {:align "center" :style "padding: 30px 0 30px 0;"}
                    [:table {:border "0" :cellpadding "0" :cellspacing "0"}
                     [:tr
                      [:td {:align "center" :style "border-radius: 25px; background-color: #ff9900; padding: 12px 30px;"}
                       [:a {:href veza-za-aktivaciju :target "_blank" :style "color: #ffffff; text-decoration: none; font-size: 18px; font-weight: bold; display: inline-block;"} "AKTIVIRAJ SVOJ NALOG ODMAH!"]]]]
                    [:p {:style "color: #999999; font-size: 12px; margin-top: 10px;"} (str "(Klikni na dugme ili kopiraj link: )" veza-za-aktivaciju)]]]
                  [:tr
                   [:td {:style "color: #333333; font-size: 16px; line-height: 1.6; padding-top: 10px;"} "### Šta dalje?
                                        Nakon aktivacije, tvoj prvi zadatak je da **postaviš oglas!** Ako imaš višak zrnaca kojima želiš da usrećiš nekoga - podeli ih! Ako su ti potrebna zrnca - pogledaj trenutnu listu oglasa!"]]
                  [:tr
                   [:td {:style "color: #007bff; font-size: 16px; font-weight: bold; line-height: 1.6; padding-top: 15px;"} "Čekamo te! Nema boljeg osećaja nego kada znaš da tvoja kefirna kultura putuje i donosi zdravlje! 💖"]]]]]
               [:tr
                [:td {:style "padding: 30px 30px 30px 30px; background-color: #e6f0ff; border-radius: 0 0 12px 12px;"}
                 [:table {:border "0" :cellpadding "0" :cellspacing "0" :width "100%"}
                  [:tr
                   [:td {:align "center" :style "color: #007bff; font-size: 14px;"} "&#169; 2025 Kefir na dar Srbija."]]]]]]]]]]])))

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
