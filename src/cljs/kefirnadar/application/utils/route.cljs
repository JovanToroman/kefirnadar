(ns kefirnadar.application.utils.route
  (:require
    [cuerdas.core :as str]))

;; region reitit code
(defprotocol IntoString
  (into-string [_]))

(extend-protocol IntoString
  string (into-string [this] this)

  Keyword (into-string [this]
            (let [ns (namespace this)]
              (str ns (if ns "/") (name this))))

  boolean (into-string [this] (str this))

  number (into-string [this] (str this))

  object (into-string [this] (str this))

  nil (into-string [_]))

(defn form-encode [s]
  (if (some? s) (str/replace (js/encodeURIComponent s) "%20" "+")))

(defn- query-parameter [k v]
  (str (form-encode (into-string k))
    "="
    (form-encode (into-string v))))

(defn query-string
  "shallow transform of query parameters into query string"
  [params]
  (->> params
    (map (fn [[k v]]
           (if (or (sequential? v) (set? v))
             (str/join "&" (map query-parameter (repeat k) v))
             (query-parameter k v))))
    (str/join "&")))
;; endregion

(defn url-for
  [url-template & {:keys [path query]}]
  (str (apply str/format url-template path)
    "?"
    (query-string query)))
