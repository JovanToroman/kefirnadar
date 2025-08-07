(ns kefirnadar.application.db)

(def default-db
  {:ads {:sharing {:form-data {}
                   :trenutno-polje-za-unos-oglasa :oblast}
         :seeking {:filters {:regions #{}
                             :seeking-milk-type? true
                             :seeking-water-type? true
                             :seeking-kombucha? true
                             :receive-by-post? true
                             :receive-in-person? true}
                   :show-filters? false}
         :registracija {:form-data {:imejl ""
                                    :lozinka ""
                                    :korisnicko-ime ""}}}})
