(ns walton.datomic-cons
  (:use [datomic.api :as d]))

(def uri "datomic:mem://cons")
(d/create-database uri)
(def conn (d/connect uri))

(def schema-txn
  [{:db/id (d/tempid :db.part/db)
    :db/ident :firstint
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :firststring
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :firstref
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :rest
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}])

@(d/transact conn schema-txn)

(defmulti attr-for class)
(defmethod attr-for String [_] :firststring)
(defmethod attr-for Long [_] :firstint)
(defmethod attr-for clojure.lang.PersistentList [_] :firstref)

(defn datomic-list [lst]
  (map-indexed
   (fn [i x]
     (merge {:db/id (d/tempid :db.part/user (- 0 (inc i))),
             (attr-for x) x}
            (when (not= i (dec (count lst)))
              {:rest (d/tempid :db.part/user (- 0 (+ 2 i)))})))
   lst))

(def list-txn
  [{:db/id (d/tempid :db.part/user -1)
    :firstint 1
    :rest (d/tempid :db.part/user -2)}
   {:db/id (d/tempid :db.part/user -2)
    :firstint 2
    :rest (d/tempid :db.part/user -3)}
   {:db/id (d/tempid :db.part/user -3)
    :firstint 3}])

(def f (d/transact conn (datomic-list '(1 2 "pants" 89))))

(def rules '[[[value-of ?f ?v]
              [?f :firstint ?v]]
             [[value-of ?f ?v]
              [?f :firststring ?v]]])

(def query '[:find ?f ?v
             :in $ %
             :where (value-of ?f ?v)])

(d/q query (db conn) rules)
