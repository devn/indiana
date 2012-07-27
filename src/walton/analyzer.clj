(ns walton.analyzer)

(defn fns-for-ns
  "Returns a sequence of strings of the public functions in a
  namespace."
  [nspace]
  (map (comp str first) (ns-publics nspace)))

(def ^{:private true} clojure-core-fns (fns-for-ns 'clojure.core))

(defn starts-with-core-fn?
  "Returns true if the form starts with a readable core function,
   else returns nil.

  Usage: (starts-with-core-fn? \"(if true 1 2)\") => true"
  [s]
  (some #(= % ((comp str first) (safe-read s))) clojure-core-fns))

(defn contains-readable-core-fn?
  "Returns true if the sexp contains a readable core function,
   else returns nil."
  [s]
  (let [safe-form-set (set (map str (flatten (safe-read s))))]
    (some safe-form-set clojure-core-fns)))
