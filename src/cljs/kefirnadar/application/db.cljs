(ns kefirnadar.application.db)

(def default-db
  {:ads {:sharing {:form-data {:firstname ""
                               :lastname ""
                               :region nil
                               :post? false
                               :pick-up? false
                               :quantity 0
                               :phone-number ""
                               :email ""
                               :sharing-milk-type? false
                               :sharing-water-type? false
                               :sharing-kombucha? false}}
         :seeking {:filters {:regions #{}
                             :seeking-milk-type? true
                             :seeking-water-type? true
                             :seeking-kombucha? true
                             :receive-by-post? true
                             :receive-in-person? true}
                   :show-filters? false}}})
