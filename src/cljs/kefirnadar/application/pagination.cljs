(ns kefirnadar.application.pagination)

(defn pagination [{:keys [change-page-redirect-url-fn page-number page-size label total-count]}]
  (let [total-number-of-pages (Math/ceil (/ total-count page-size))]
    [:nav {:aria-label label}
     [:ul.pagination
      [:li.page-item {:class (when (= page-number 1) "disabled")}
       [:a.page-link {:href (change-page-redirect-url-fn (dec page-number) page-size)} "Prethodna"]]
      ;;[:li.page-item [:button.page-link {:on-click (change-page-function (dec page-number))} "1"]]
      ;;[:li.page-item [:button.page-link {:on-click "#"} "2"]]
      ;;[:li.page-item [:button.page-link {:on-click "#"} "3"]]
      [:li.page-item {:class (when (= page-number total-number-of-pages) "disabled")}
       [:a.page-link {:href (change-page-redirect-url-fn (inc page-number) page-size)} "Sledeća"]]]]))
