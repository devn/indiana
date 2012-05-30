(ns walton.db
  (:use [korma core db]))

(def config
  (try (read-string (slurp "walton.conf"))
       (catch Exception _ {:db "getclojure", :user "plato"})))

(defdb db (postgres config))

(defentity expressions
  (database db)
  (entity-fields :input :value :out))

(defn insert-expression [{:keys [input value out]}]
  (insert expressions
    (values {:input input :value value :out out})))

(defn all-passing-sexps []
  (select expressions))

(defn wildcardize-query [s]
  (str "%" s "%"))

(defn exprs-where [k s]
  (select expressions (where {k s})))

(defn exprs-where-like [k s]
  (select expressions (where {k [like (wildcardize-query s)]})))

(defn exprs-where-input [s]
  (exprs-where-like :input s))

(defn exprs-where-input-eq [s]
  (exprs-where :input s))

(defn exprs-where-value [s]
  (exprs-where-like :value s))

(defn exprs-where-value-eq [s]
  (exprs-where :value s))

(defn exprs-where-out [s]
  (exprs-where-like :out s))

(defn exprs-where-out-eq [s]
  (exprs-where :out s))
