(ns kefirnadar.application.views
  (:require [kefirnadar.application.events :as events]
            [kefirnadar.application.subscriptions :as subs]
            [re-frame.core :refer [dispatch subscribe]]))

(def regions [:Ада
              :Александровац
              :Алексинац
              :Алибунар
              :Апатин
              :Аранђеловац
              :Ариље
              :Бабушница
              :Бајина-Башта
              :Баточина
              :Бач
              :Бачка-Паланка
              :Бачка-Топола
              :Бачки-Петровац
              :Београд
              :Бела-Паланка
              :Бела-Црква
              :Беочин
              :Бечеј
              :Блаце
              :Богатић
              :Бор
              :Бојник
              :Бољевац
              :Босилеград
              :Брус
              :Бујановац
              :Варварин
              :Ваљево
              :Врање
              :Вршац
              :Велика-Плана
              :Велико-Градиште
              :Витина
              :Владимирци
              :Владичин-Хан
              :Власотинце
              :Врбас
              :Врњачка-Бања
              :Вучитрн
              :Гаџин-Хан
              :Глоговац
              :Гњилане
              :Голубац
              :Гора
              :Горњи-Милановац
              :Деспотовац
              :Дечани
              :Димитровград
              :Дољевац
              :Ђаковица
              :Жабаљ
              :Жабари
              :Жагубица
              :Житиште
              :Житорађа
              :Звечан
              :Зајечар
              :Зрењанин
              :Зубин-Поток
              :Ивањица
              :Инђија
              :Ириг
              :Исток
              :Јагодина
              :Кикинда
              :Крагујевац
              :Краљево
              :Крушевац
              :Кањижа
              :Качаник
              :Кладово
              :Клина
              :Кнић
              :Књажевац
              :Ковачица
              :Ковин
              :Косјерић
              :Косово-Поље
              :Косовска-Каменица
              :Косовска-Митровица
              :Коцељева
              :Крупањ
              :Кула
              :Куршумлија
              :Кучево
              :Лесковац
              :Лозница
              :Лајковац
              :Лапово
              :Лебане
              :Лепосавић
              :Липљан
              :Лучани
              :Љиг
              :Љубовија
              :Мајданпек
              :Мали-Зворник
              :Мали-Иђош
              :Мало-Црниће
              :Медвеђа
              :Мерошина
              :Мионица
              :Неготин
              :Ниш
              :Нови-Пазар
              :Нови-Сад
              :Нова-Варош
              :Нова-Црња
              :Нови-Бечеј
              :Нови-Кнежевац
              :Ново-Брдо
              :Обилић
              :Опово
              :Ораховац
              :Осечина
              :Оџаци
              :Параћин
              :Панчево
              :Пирот
              :Пожаревац
              :Приштина
              :Петровац-на-Млави
              :Пећ
              :Пећинци
              :Пландиште
              :Подујево
              :Прокупље
              :Пожега
              :Прешево
              :Прибој
              :Призрен
              :Пријепоље
              :Ражањ
              :Рача
              :Рашка
              :Рековац
              :Рума
              :Свилајнац
              :Сврљиг
              :Смедерево
              :Сомбор
              :Сента
              :Сечањ
              :Сјеница
              :Смедеревска-Паланка
              :Сокобања
              :Србица
              :Србобран
              :Сремски-Карловци
              :Сремска-Митровица
              :Стара-Пазова
              :Суботица
              :Сува-Река
              :Сурдулица
              :Темерин
              :Тител
              :Топола
              :Трговиште
              :Трстеник
              :Тутин
              :Ћићевац
              :Ћуприја
              :Уб
              :Ужице
              :Урошевац
              :Црна-Трава
              :Чајетина
              :Чачак
              :Чока
              :Шабац
              :Шид
              :Штимље
              :Штрпце])

;; -- helper functions region --
(defn extract-input-value
  [event]
  (-> event .-target .-value))

(defn extract-checkbox-state
  [event]
  (-> event .-target .-checked))
;; -- end helper functions region --


;; form region ( case :ad-type :sharing )
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
              :on-change   #(dispatch [::events/update-form id (extract-input-value %)])
              :type        "number"
              :placeholder "Kolicina koju delite..."}]]))

(defn form []
  (let [is-valid? @(subscribe [::subs/is-valid? [:first-name :last-name :place :quantity]])]
    [:div
     [first-name-input :first-name]
     [last-name-input :last-name]
     [region-select :place regions]
     [:div
      "Nacini transakcije:"
      [post-toggle :post]
      [pick-up-toggle :pick-up]]
     [qty-input :quantity]
     [:div
      [:button {:disabled (not is-valid?)
                :on-click #(dispatch [::events/save-form])} "Sacuvaj"]]]))

;; end form region

;; Ovo cu promeniti, napravio sam ovako samo da bi video da li mi radi..
(defn single-user
  "A single user."
  [_user]
  (fn [{:keys [db/id
               user/firstname
               user/lastname
               user/region
               user/post
               user/pick-up
               user/quantity
               ] :as user}]
    [:li
     [:div
      [:spam id]
      [:spam " "]
      [:spam firstname]
      [:spam " "]
      [:spam lastname]
      [:spam " "]
      [:spam region]
      [:spam " "]
      [:spam post]
      [:spam " "]
      [:spam pick-up]
      [:spam " "]
      [:spam quantity]]]))

(defn users-list
  "List of all users."
  [users]
  [:ul
   (for [user users]
     [single-user user])])


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
    :sharing [form]
    :seeking [users-list @(subscribe [::subs/users])]))

(defn thank-you []
  [:div [:h1 "Hvala vam sto delite kefir zrnca"]
   [:button {:on-click #(dispatch [::events/dispatch-load-route! {:data {:name :route/home}}])} "Pocetna stranica"]])








