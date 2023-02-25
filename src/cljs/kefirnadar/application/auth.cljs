(ns kefirnadar.application.auth
  (:require [kefirnadar.application.specs :as specs]
            [applied-science.js-interop :as j]
            [re-frame.core :refer [reg-event-db trim-v reg-sub dispatch]]))

(defprotocol AuthMethod
  (get-login-status [this] "Checks whether the current user is logged in with this auth method")
  (handle-login-response [this response] "Finish logging the user in if they were authenticated by the auth provider")
  (log-user-in [this] "Logs the user in using the auth method"))

(defrecord FacebookLogin [] AuthMethod
  (get-login-status [this]
    (j/call js/FB :getLoginStatus #(handle-login-response this %)))
  (handle-login-response [_this response]
    (let [{:keys [status authResponse] :as rsp}
          ^::specs/facebook-get-login-status-response (update (js->clj response :keywordize-keys true) :status keyword)]
      (js/console.log "Response: " rsp ". Acting now")
      (dispatch [::set-authentication-data (cond-> {:status status
                                                    :authenticated? (case status
                                                                      :connected true
                                                                      :unknown false
                                                                      false)}
                                             (some? authResponse) (assoc :user-info (js->clj authResponse
                                                                                      :keywordize-keys true)))])))
  (log-user-in [this]
    (j/call js/FB :login #(handle-login-response this %))))

(def auth-methods {:facebook (->FacebookLogin)})

;; TODO: figure out how to check if a user is logged in. This way of doing it would not work for multiple login methods
;; since subsequent login methods would overwrite the result of the previous ones in app db.
(defn get-authentication-statuses! []
  (doseq [auth-method (vals auth-methods)]
    (get-login-status auth-method)))

;; region events
(reg-event-db ::set-authentication-data trim-v
  (fn [db [authentication-info]]
    (assoc db :auth authentication-info)))

(reg-event-db ::log-user-in trim-v
  (fn [_db [authentication-provider]]
    (log-user-in (authentication-provider auth-methods))))
;; endregion

;; region subs
(reg-sub ::authenticated?
  (fn [db _]
    (get-in db [:auth :authenticated?])))

(reg-sub ::user-info
  (fn [db _]
    (get-in db [:auth :user-info])))

(reg-sub ::user-id :<- [::user-info]
  (fn [user-info _]
    (:userID user-info)))
;; endregion