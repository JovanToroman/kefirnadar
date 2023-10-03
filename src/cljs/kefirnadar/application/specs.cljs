(ns kefirnadar.application.specs
  (:require [cljs.spec.alpha :as s]))

(s/def ::user-action #{:sharing :seeking})
(s/def ::status #{:unknown :connected})
(s/def ::authResponse (s/keys :req-un [::accessToken ::expiresIn ::signedRequest ::userID]))
(s/def ::facebook-get-login-status-response (s/keys :req-un [::status] :opt-un [::authResponse]))
(s/def ::filters (s/keys :opt-un [::grains-kinds ::regions ::handover-types]))
(s/def ::pagination-info (s/keys :opt-un [::page-number ::page-size]))
(s/def ::podaci-korisnika (s/keys :req-un [::imejl ::lozinka] :opt-un [::korisnicko-ime]))