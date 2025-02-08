(ns kefirnadar.application.subscriptions
  (:require [re-frame.core :refer [trim-v reg-event-db reg-event-fx reg-sub]]))

(reg-sub ::active-route
  (fn [db _]
    (:active-route db)))

(reg-sub ::sharing-form-data (fn [db _] (get-in db [:ads :sharing :form-data])))

(reg-sub ::form-field
  (fn [db [_ id]]
    (get-in db [:ads :sharing :form-data id])))

(reg-sub ::form-validation
  (fn [db [_ id]]
    (get-in db [:ads :sharing :form-data-validation id])))

(reg-sub ::polje-forme-registracije
  (fn [db [_ id]]
    (get-in db [:ads :registracija :form-data id])))

(reg-sub ::provera-forme-registracije
  (fn [db [_ id]]
    (get-in db [:ads :registracija :form-data-validation id])))

(reg-sub ::polje-forme-prijave
  (fn [db [_ id]]
    (get-in db [:ads :prijava :form-data id])))

(reg-sub ::provera-forme-prijave
  (fn [db [_ id]]
    (get-in db [:ads :prijava :form-data-validation id])))

(reg-sub ::polje-forme-za-slanje-imejla-za-resetovanje-lozinke
  (fn [db [_ id]]
    (get-in db [:ads :slanje-imejla-za-resetovanje-lozinke :form-data id])))

(reg-sub ::provera-forme-za-slanje-imejla-za-resetovanje-lozinke
  (fn [db [_ id]]
    (get-in db [:ads :slanje-imejla-za-resetovanje-lozinke :form-data-validation id])))

(reg-sub ::polje-forme-za-resetovanje-lozinke
  (fn [db [_ id]]
    (get-in db [:ads :resetovanje-lozinke :form-data id])))

(reg-sub ::provera-forme-za-resetovanje-lozinke
  (fn [db [_ id]]
    (get-in db [:ads :resetovanje-lozinke :form-data-validation id])))

(reg-sub ::polje-kontakt-forme
  (fn [db [_ id]]
    (get-in db [:ads :kontakt :podaci-forme id])))

(reg-sub ::provera-polja-kontakt-forme
  (fn [db [_ id]]
    (get-in db [:ads :kontakt :provera-podataka-forme id])))

(reg-sub ::kod-greske
  (fn [db [_ stranica]]
    (get-in db [:ads stranica :greska])))

(reg-sub ::filters
  (fn [db [_ filter-key]] (get-in db (cond-> [:ads :seeking :filters]
                                       (some? filter-key) (conj filter-key)))))

(reg-sub ::ads-meta
  (fn [db [_ ad-id meta-key]] (get-in db (cond-> [:ads :seeking :ads-meta ad-id]
                                           (keyword? meta-key) (conj meta-key)))))

(reg-sub ::filtered-ads
  (fn [db _] (get-in db [:ads :seeking :filtered-ads :ads])))

(reg-sub ::ads-count
  (fn [db _] (get-in db [:ads :seeking :filtered-ads :ads-count])))

(reg-sub ::ads-pagination-info
  (fn [db _]
    (get-in db [:ads :seeking :pagination-info])))

(reg-sub ::show-filters?
  (fn [db _]
    (get-in db [:ads :seeking :show-filters?])))

(reg-sub ::aktivacioni-kod-poslat?
  (fn [db _]
    (get-in db [:auth :aktivacioni-kod-poslat?])))

(reg-sub ::moji-oglasi
  (fn [db _]
    (get-in db [:ads :moji-oglasi :ads])))
