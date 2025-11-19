(ns kefirnadar.common.prikazi)

(defn glava []
  [:head
   [:meta {:charset "UTF-8"}]
   [:title "Kefir na dar - platforma za razmenu zrnaca mlečnog i vodenog kefira i kombuhe"]
   [:meta {:name "description" :content "Veb aplikacija za deljenje i traženje zrnaca mlečnog i vodenog kefira i kombuhe (kombuća)"}]
   [:link {:href "https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" :rel "stylesheet" :integrity "sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" :crossorigin "anonymous"}]
   [:link {:rel "icon" :href "/favicon.ico"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"}]
   [:script {:async "true" :src "https://www.googletagmanager.com/gtag/js?id=G-9X95EQ2X86"}]
   [:script "window.dataLayer = window.dataLayer || [];
        function gtag(){dataLayer.push(arguments);}
        gtag('js', new Date());

        gtag('config', 'G-9X95EQ2X86');"]
   [:script "window.fbAsyncInit = function() {
            FB.init({
                appId      : '1869115913469264',
                cookie     : true,
                xfbml      : true,
                version    : 'v23.0'
            });

            FB.AppEvents.logPageView();

        };

        (function(d, s, id){
            var js, fjs = d.getElementsByTagName(s)[0];
            if (d.getElementById(id)) {return;}
            js = d.createElement(s); js.id = id;
            js.src = \"https://connect.facebook.net/en_US/sdk.js\";
            fjs.parentNode.insertBefore(js, fjs);
        }(document, 'script', 'facebook-jssdk'));"]])

(defn pocetna-strana []
  [:div#app.min-vh-100
   [:div.container
    [:nav.navbar.navbar-expand-lg.navbar-light.bg-light.rounded
     [:div.container-fluid [:a.navbar-brand {:href "/"} [:img {:src "/images/logo.avif"
                                                               :style {:width "10em" :height "10em"}}]]
      [:button.navbar-toggler {:type "button" :data-bs-toggle "collapse" :data-bs-target "#navbarSupportedContent" :aria-controls "navbarSupportedContent" :aria-expanded "false" :aria-label "Toggle navigation"} [:span.navbar-toggler-icon]]
      [:div#navbarSupportedContent.collapse.navbar-collapse
       [:ul.navbar-nav.me-auto.mb-2.mb-lg-0
        [:li.nav-item [:a.nav-link.active {:aria-current "page" :href "/"} "Početna"]]
        [:li.nav-item [:a.nav-link.active {:aria-current "page" :href "https://blog.kefirnadar.rs/"} "Blog"]]
        [:li.nav-item [:a.nav-link.mr-2.active {:href "/kontakt"} "Kontakt"]]
        [:li.nav-item [:a.nav-link.mr-2.active {:href "/politika-privatnosti"} "Politika
                            privatnosti"]]]
       [:ul.navbar-nav.ms-auto.mb-2.mb-lg-0.justify-content-end
        [:li.nav-item.ms-auto [:a.nav-link.active {:href "/prijava"} "Prijavi se"]]]]]]
    [:div.d-flex.flex-column.justify-content-center.align-items-center.pt-5 {:style {:min-height "80vh"}}
     [:div.row.flex-lg-row-reverse.align-items-center.g-5
      [:div.col-10.col-sm-8.col-lg-6 [:img.d-block.mx-lg-auto.img-fluid {:src "/images/heroj.avif" :alt "srbija-širi-kefir" :width "700" :height "500" :loading "lazy"}]]
      [:div.col-lg-6 [:h1.display-5.fw-bold.lh-1.mb-3 "Dobrodošli u Kefir na dar, aplikaciju za
                    deljenje i potražnju zrnaca mlečnog i vodenog kefira i kombuhe"]
       [:p.lead "Kefir na dar je jedina aplikacija u Srbiji i regionu za deljenje zrnaca mlečnog i
                        vodenog kefira i kombuhe. Ako ne znate kako da dobijete zrnca, možete nam se slobodno obratiti
                        putem stranice " [:a {:href "/kontakt"} "Kontakt"] ". Ako želite da sačuvate oglase koje
                        postavite tako da im možete pristupiti kasnije, predlažemo da se " [:a {:href "/registracija"} "registrujete"] " pre postavljanja oglasa. Ako nemate vremena za gubljenje i želite što pre da dođete do svoje
                        kulture, pritisnite dugme ispod i stupite u kontakt sa nekim od naših delilaca. Srećno
                        fermentisanje! 🥳 🥛"]
       [:div.d-grid.gap-2.d-md-flex.justify-content-md-start
        [:button.btn.btn-primary.btn-lg.px-4.me-md-2 "Podeli kulturu"]
        [:button.btn.btn-outline-secondary.btn-lg.px-4 "Nađi kulturu"]]]]]
    [:p.copyright-text.mt-5.d-flex.justify-content-center "Copyright © 2022-2025 All Rights Reserved by Do
            Brave Plus Software"]]
   [:script {:src "/js/main.js?v=123456"}]
   [:script "kefirnadar.configuration.core.start();"]
   [:script {:src "https://kit.fontawesome.com/59fc276a52.js" :crossorigin "anonymous"}]
   [:script {:src "https://code.jquery.com/jquery-3.2.1.slim.min.js" :integrity "sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" :crossorigin "anonymous"}]
   [:script {:src "https://cdn.jsdelivr.net/npm/@popperjs/core@2.9.2/dist/umd/popper.min.js" :integrity "sha384-IQsoLXl5PILFhosVNubq5LC7Qb9DXgDA9i+tQ8Zj3iwWAwPtgFTxbJ8NT4GN1R8p" :crossorigin "anonymous"}]
   [:script {:src "https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.min.js" :integrity "sha384-cVKIPhGWiC2Al4u+LWgxfKTRIcfu0JTxR+EQDz/bgldoEyl4H0zUF0QKbrJ0EcQF" :crossorigin "anonymous"}]])
