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

(defn find-examples-by-input [s]
  (select expressions (where {:input [like (wildcardize-query s)]})))

(defn find-examples-where-input-eq [s]
  (select expressions (where {:input s})))

(defn find-examples-where-val-eq [s]
  (select expressions (where {:value s})))

(defn find-examples-by-value [s]
  (select expressions (where {:value [like (wildcardize-query s)]})))

(defn find-examples-by-out [s]
  (select expressions (where {:out [like (wildcardize-query s)]})))

(defn find-examples-where-out-eq [s]
  (select expressions (where {:out s})))

(defn find-examples-by-value-or-out [s]
  (select expressions
    (where (or {:value [like (wildcardize-query s)]}
               {:out [like (wildcardize-query s)]}))))

(defn find-examples-where-val-or-out-eq [s]
  (select expressions
    (where (or {:value s}
               {:out s}))))
