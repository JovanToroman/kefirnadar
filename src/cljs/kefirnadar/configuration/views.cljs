(ns kefirnadar.configuration.views
  (:require
    [kefirnadar.application.styles :as styles]
    [kefirnadar.application.views :as application-views]
    [kefirnadar.configuration.subscriptions :as subscriptions]
    [re-frame.core :refer [subscribe]]))

(defn- panels [panel-name]
  (case (:name (:data panel-name))
    :route/home [application-views/home]
    ;; -----
    :route/ad-type [application-views/grains-kind]
    ;; -----
    :route/ad-type-choice [application-views/ad-type-choice]
    ;; -----
    :route/thank-you [application-views/thank-you]
    ;; -----
    :route/error [application-views/error]
    [:div]))

(defn main-panel []
  (let [active-panel @(subscribe [::subscriptions/active-route])
        [css] (styles/use-styletron)]
    [:div {:className (css (:wrapper styles/styles-map))}                                         ;; NE RADI DROPDOWN!!!????????????????????
     ;; NAVBAR
     [:nav.navbar.navbar-expand-lg.navbar-light.bg-light
      {:style {:height "5wh"}}
      [:a.navbar-brand {:href "#"} "Kefir na Dar"]
      [:button.navbar-toggler {:type "button" :data-toggle "collapse" :data-target "#navbarNav" :aria-controls "navbarNav" :aria-expanded "false" :aria-label "Toggle navigation"}
       [:span.navbar-toggler-icon]]
      [:div#navbarNav.collapse.navbar-collapse
       [:ul.navbar-nav
        [:li.nav-item.active
         [:a.nav-link {:href "#"} "Home" [:span.sr-only "(current)"]]]
        [:li.nav-item
         [:a.nav-link {:href "#"} "Features"]]
        [:li.nav-item
         [:a.nav-link {:href "#"} "Pricing"]]
        [:li.nav-item
         [:a.nav-link.disabled {:href "#"} "Disabled"]]]]]
     ;; CONTENT
     [:div.mt-5
      [panels active-panel]]
     ;; FOOTER
     [:footer.site-footer
      [:div.container
       [:div.row
        [:div.col-sm-12.col-md-6
         [:h6 "About"]
         [:p.text-justify "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras non dapibus lectus. Etiam in nunc malesuada, cursus metus id, gravida lorem. Nullam sem enim, pretium quis suscipit eget, eleifend vitae dui. Aliquam interdum urna eget mattis scelerisque. Duis condimentum ligula arcu, vitae placerat quam convallis non. Aliquam erat volutpat. Ut."]]
        [:div.col-xs-6.col-md-3
         [:h6 "Categories"]
         [:ul.footer-links
          [:li [:a {:href "#"} "Link 1"]]
          [:li [:a {:href "#"} "Link 2"]]
          [:li [:a {:href "#"} "Link 3"]]
          [:li [:a {:href "#"} "Link 4"]]
          [:li [:a {:href "#"} "Link 5"]]
          [:li [:a {:href "#"} "Link 6"]]]]
        [:div.col-xs-6.col-md-3
         [:h6 "Quick Links"]
         [:ul.footer-links
          [:li [:a {:href "#"} "About Us"]]
          [:li [:a {:href "#"} "Contact Us"]]
          [:li [:a {:href "#"} "Contribute"]]
          [:li [:a {:href "#"} "Privacy Policy"]]
          [:li [:a {:href "#"} "Sitemap"]]]]]
       [:hr]]
      [:div.container
       [:div.row
        [:div.col-md-8.col-sm-6.col-xs-12
         [:p.copyright-text "Copyright &copy; 2022 All Rights Reserved by"
          [:a {:href "#"} " ........"] "."]]]]]]))



