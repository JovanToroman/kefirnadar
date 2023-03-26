(ns kefirnadar.configuration.postgres
  (:require [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [honey.sql :as sql]
            [kefirnadar.configuration.config :as config]
            [taoensso.timbre :as log])
  (:import [com.zaxxer.hikari HikariDataSource]))

(defonce  pooled (atom nil))

(defn pg-conn [] @pooled)

(defn datasource [] (:datasource (pg-conn)))

(defn stop! []
  (.close (datasource)))

(defn execute-transaction!
  [query]
  (jdbc/execute-one! (pg-conn)
    (sql/format query)))

(defn execute-query!
  [query]
  (jdbc/execute! (pg-conn)
    (sql/format query)))

(defn execute-one!
  [query]
  (jdbc/execute-one! (pg-conn)
    (sql/format query)))

(defn start! [schema]
  (log/info "starting db...")
  (reset! pooled (connection/->pool HikariDataSource @config/postgres-main))
  (jdbc/execute-one! (pg-conn) [schema]))
