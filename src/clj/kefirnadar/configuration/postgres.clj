(ns kefirnadar.configuration.postgres
  (:require [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [honey.sql :as sql]
            [kefirnadar.configuration.config :as config]
            [taoensso.timbre :as log])
  (:import [com.zaxxer.hikari HikariDataSource]))

(def ^{:private true} pooled (atom nil))

(defn opp-pg-conn [] @pooled)

(defn stop! []
  (.close (:datasource @pooled)))

(defn execute-transaction!
  [query]
  (jdbc/execute-one! (opp-pg-conn)
    (sql/format query)))

(defn execute-query!
  [query]
  (jdbc/execute! (opp-pg-conn)
    (sql/format query)))

(defn start! [schema]
  (log/info "starting db...")
  (reset! pooled (connection/->pool HikariDataSource config/postgres))
  (jdbc/execute-one! (opp-pg-conn) [schema]))
