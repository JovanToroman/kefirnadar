(ns kefirnadar.application.specs
  (:require [cljs.spec.alpha :as s]))

(s/def ::user-action #{:sharing :seeking})
(s/def ::status #{:unknown :connected})
(s/def ::authResponse (s/keys :req-un [::accessToken ::expiresIn ::signedRequest ::userID]))
(s/def ::facebook-get-login-status-response (s/keys :req-un [::status] :opt-un [::authResponse]))