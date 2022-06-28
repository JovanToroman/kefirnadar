(ns kefirnadar.application.views
  (:require [kefirnadar.application.events :as events]
            [kefirnadar.application.subscriptions :as subs]
            [re-frame.core :refer [dispatch subscribe]]
            [goog.string :as gstr]))

;; TODO: 1. Add a phone number and email address for a user. DONE
;; TODO  2. Add a 30 day timer so users that are older then that are deleted. OUR DB IS CURRENTLY MEM TYPE, POSTPONED
;; TODO  3. Seeker needs to press "show phone number" button so we can prevent information fishing.
;; TODO  4. (MAYBE) Change region vector values to strings, is there need for them to be keywords?
;; TODO  5. (MAYBE) Create a few helper functions for stuff we use more then few times if possible..

;; TODO-CHECK 1. Why are :path-params values stored as strings when i try to store them as keywords???
;; TODO-CHECK 2. Maybe we can lower the need of app-db if we store more information in routes???



(def regions [:Ada
              :Aleksandrovac
              :Aleksinac
              :Alibunar
              :Apatin
              :Aranđelovac
              :Arilje
              :Babušnica
              :Bajina-Bašta
              :Batočina
              :Bač
              :Bačka-Palanka
              :Bačka-Topola
              :Bački-Petrovac
              :Beograd
              :Bela-Palanka
              :Bela-Crkva
              :Beočin
              :Bečej
              :Blace
              :Bogatić
              :Bor
              :Bojnik
              :Boljevac
              :Bosilegrad
              :Brus
              :Bujanovac
              :Varvarin
              :Valjevo
              :Vranje
              :Vršac
              :Velika-Plana
              :Veliko-Gradište
              :Vitina
              :Vladimirci
              :Vladičin-Han
              :Vlasotince
              :Vrbas
              :Vrnjačka-Banja
              :Vučitrn
              :Gadžin-Han
              :Glogovac
              :Gnjilane
              :Golubac
              :Gora
              :Gornji-Milanovac
              :Despotovac
              :Dečani
              :Dimitrovgrad
              :Doljevac
              :Đakovica
              :Žabalj
              :Žabari
              :Žagubica
              :Žitište
              :Žitorađa
              :Zvečan
              :Zaječar
              :Zrenjanin
              :Zubin-Potok
              :Ivanjica
              :Inđija
              :Irig
              :Istok
              :Jagodina
              :Kikinda
              :Kragujevac
              :Kraljevo
              :Kruševac
              :Kanjiža
              :Kačanik
              :Kladovo
              :Klina
              :Knić
              :Knjaževac
              :Kovačica
              :Kovin
              :Kosjerić
              :Kosovo-Polje
              :Kosovska-Kamenica
              :Kosovska-Mitrovica
              :Koceljeva
              :Krupanj
              :Kula
              :Kuršumlija
              :Kučevo
              :Leskovac
              :Loznica
              :Lajkovac
              :Lapovo
              :Lebane
              :Leposavić
              :Lipljan
              :Lučani
              :Ljig
              :Ljubovija
              :Majdanpek
              :Mali-Zvornik
              :Mali-Iđoš
              :Malo-Crniće
              :Medveđa
              :Merošina
              :Mionica
              :Negotin
              :Niš
              :Novi-Pazar
              :Novi-Sad
              :Nova-Varoš
              :Nova-Crnja
              :Novi-Bečej
              :Novi-Kneževac
              :Novo-Brdo
              :Obilić
              :Opovo
              :Orahovac
              :Osečina
              :Odžaci
              :Paraćin
              :Pančevo
              :Pirot
              :Požarevac
              :Priština
              :Petrovac-na-Mlavi
              :Peć
              :Pećinci
              :Plandište
              :Podujevo
              :Prokuplje
              :Požega
              :Preševo
              :Priboj
              :Prizren
              :Prijepolje
              :Ražanj
              :Rača
              :Raška
              :Rekovac
              :Ruma
              :Svilajnac
              :Svrljig
              :Smederevo
              :Sombor
              :Senta
              :Sečanj
              :Sjenica
              :Smederevska-Palanka
              :Sokobanja
              :Srbica
              :Srbobran
              :Sremski-Karlovci
              :Sremska-Mitrovica
              :Stara-Pazova
              :Subotica
              :Suva-Reka
              :Surdulica
              :Temerin
              :Titel
              :Topola
              :Trgovište
              :Trstenik
              :Tutin
              :Ćićevac
              :Ćuprija
              :Ub
              :Užice
              :Uroševac
              :Crna-Trava
              :Čajetina
              :Čačak
              :Čoka
              :Šabac
              :Šid
              :Štimlje
              :Štrpce])

;; -- helper functions region --
(defn extract-input-value
  [event]
  (-> event .-target .-value))

(defn extract-checkbox-state
  [event]
  (-> event .-target .-checked))

#_(defn hidden?                                             ;;make this work
  [hidden type user]
  (case hidden
    false [:button {:on-click #(hidden? true type user)} "Prikazi " type]
    true (:user/phone-number user)))
;; -- end helper functions region --


(defn first-name-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Ime"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :type        "text"
              :placeholder "Vase ime..."}]]))


(defn last-name-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Prezime"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :type        "text"
              :placeholder "Vase prezime..."}]]))


(defn region-select [id regions]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label " Opstina: "]
     [:div
      [:select {:value     @value
                :on-change #(dispatch [::events/update-form id (keyword (extract-input-value %))])}
       [:option {:value ""} "Izabarite opstinu"]
       (map (fn [r] [:option {:key r :value r} r]) regions)]]]))

(defn email-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "E-mail"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :type        "text"
              :placeholder "Vasa elektronska posta..."}]]))

(defn phone-number-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Telefon"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :type        "text"
              :placeholder "Vas broj telefona..."}]]))

(defn post-toggle [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Razmena postom?"]
     [:input {:on-change #(dispatch [::events/update-form id (extract-checkbox-state %)])
              :type      "checkbox"
              :checked   @value}]]))


(defn pick-up-toggle [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label "Razmena uzivo?"]
     [:input {:on-change #(dispatch [::events/update-form id (extract-checkbox-state %)])
              :type      "checkbox"
              :checked   @value}]]))


(defn qty-input [id]
  (let [value (subscribe [::subs/form id])]
    [:div
     [:label {:for "qty"} "Koju kolicinu delite?"]
     [:input {:value       @value
              :on-change   #(dispatch [::events/update-form id (long (extract-input-value %))])
              :type        "number"
              :placeholder "Kolicina koju delite..."}]]))


(defn form []
  (let [is-valid? @(subscribe [::subs/is-valid? [:firstname :lastname :region :quantity]])]
    [:div
     [first-name-input :firstname]
     [last-name-input :lastname]
     [region-select :region regions]
     [:div
      "Kontakt informacije:"
      [phone-number-input :phone-number]
      [email-input :email]]
     [:div
      "Nacini transakcije:"
      [post-toggle :post]
      [pick-up-toggle :pick-up]]
     [qty-input :quantity]
     [:div
      [:button {:disabled (not is-valid?)
                :on-click #(dispatch [::events/create])} "Sacuvaj"]]]))


;; Ovo cu promeniti, napravio sam ovako samo da bi video da li mi radi..
(defn user-row
  "A single user."
  [_users _reg-val]
  (for [user _users]
    [:tbody
     [:tr {:align "left"
           :style {:border "1px solid black"
                   :width  "100%"}}
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (:user/firstname user)]
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (:user/lastname user)]
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (:user/region user)]
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (:user/phone-number user)]
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (:user/email user)]
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (if (:user/post user) (gstr/unescapeEntities "&#10004") (gstr/unescapeEntities "&#10007"))]
      [:td {:style {:text-align "center"
                    :border     "1px solid black"}} (if (:user/pick-up user) (gstr/unescapeEntities "&#10004") (gstr/unescapeEntities "&#10007"))]]]))


(defn users-list
  "List of all users."
  []
  (let [region-value (subscribe [::subs/region])
        users (subscribe [::subs/users])]
    [:div
     [:button {:on-click #(dispatch [::events/clean-db-and-go-home])} "Pocetna stranica"]
     [:div
      [:label " Opstina: "]
      [:div
       [:select {:value     @region-value
                 :on-change #(dispatch [::events/fetch-users (keyword (extract-input-value %))])}
        [:option {:value ""} "Izabarite opstinu"]
        (map (fn [r] [:option {:key r :value r} r]) regions)]]]
     (when @users
       [:table {:style {:border          "1px solid black"
                        :border-collapse "collapse"
                        :width           "100%"}}
        [:tbody
         [:tr {:style {:width "100%"}}
          [:th {:style {:border "1px solid black"}} "Ime"]
          [:th {:style {:border "1px solid black"}} "Prezime"]
          [:th {:style {:border "1px solid black"}} "Region"]
          [:th {:style {:border "1px solid black"}} "Telefon"]
          [:th {:style {:border "1px solid black"}} "E-mail"]
          [:th {:style {:border "1px solid black"}} "Slanje postom"]
          [:th {:style {:border "1px solid black"}} "Licno preuzimanje"]]]
        (user-row @users @region-value)])]))


(defn home []
  [:div [:h1 "Da li delite ili trazite kefir?"]
   [:button {:on-click #(dispatch [::events/ad-type {:type :sharing}])} "Delim"]
   [:button {:on-click #(dispatch [::events/ad-type {:type :seeking}])} "Trazim"]])


(defn grains-kind []
  [:div
   [:button {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :milk-type} "Mlecni"]
   [:button {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :water-type} "Vodeni"]
   [:button {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :kombucha} "Kombuha"]])


(defn ad-type-choice []
  (case @(subscribe [::subs/ad-type-choice])
    :sharing form
    :seeking users-list))

(defn thank-you []
  [:div [:h1 "Hvala vam sto delite kefir zrnca"]
   [:button {:on-click #(dispatch [::events/clean-db-and-go-home])} "Pocetna stranica"]])

(defn error []
  [:div
   [:h1 "ERROR PAGE"]
   [:button {:on-click #(dispatch [::events/clean-db-and-go-home])} "Pocetna stranica"]])

