(ns kefirnadar.application.views
  (:require [kefirnadar.application.events :as events]
            [kefirnadar.application.subscriptions :as subs]
            [re-frame.core :refer [dispatch subscribe]]
            [goog.string :as gstr]
            [kefirnadar.application.localstorage :as localstorage]))

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
  ;; Here we want to dispatch detail-user (route where we will display all single user details)
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
                 :on-change #(dispatch [::events/fetch-users (keyword (localstorage/get-item :grains-kind)) (keyword (extract-input-value %))])}
        [:option {:value ""} "Izabarite opstinu"]
        (map (fn [r] [:option {:key r :value r} r]) regions)]]]
     [:table {:style {:border          "1px solid black"
                      :border-collapse "collapse"
                      :width           "100%"}}
      [:tbody
       [:tr {:style {:width  "100%"}}
       [:th {:style {:border "1px solid black"}} "Ime"]
       [:th {:style {:border "1px solid black"}} "Prezime"]
       [:th {:style {:border "1px solid black"}} "Region"]
       [:th {:style {:border "1px solid black"}} "Slanje postom"]
       [:th {:style {:border "1px solid black"}} "Licno preuzimanje"]]]
      (user-row @users @region-value)]]))


(defn home []
  [:div [:h1 "Da li delite ili trazite kefir?"]
   [:button {:on-click #(dispatch [::events/ad-type {:type :sharing}])} "Delim"]
   [:button {:on-click #(dispatch [::events/ad-type {:type :seeking}])} "Trazim"]])

(defn grains-kind []
  [:div
   [:button {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :milk-type} "Mlecni"]
   [:button {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :water-type} "Vodeni"]
   [:button {:on-click #(dispatch [::events/grains-kind (extract-input-value %)]) :value :kombucha} "Kombuha"]])

(defn choice []
  (case @(subscribe [::subs/choice])
    :sharing #(dispatch [::events/dispatch-load-route! {:data {:name :route/form}}])
    :seeking #(dispatch [::events/dispatch-load-route! {:data {:name :route/list}}])))


(defn thank-you []
  [:div [:h1 "Hvala vam sto delite kefir zrnca"]
   [:button {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Pocetna stranica"]])

(defn error []
  [:div
   [:h1 "ERROR PAGE"]
   [:button {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Pocetna stranica"]])



;; napraviti rutu seeking/:grains-kind/:region, iz nje nestujemo rutu /list (u njoj kao parametre izvucemo grains-kind i region
;; svaki put kada se promeni region pozivamo ponovo :seeking rutu)




