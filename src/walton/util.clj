(ns walton.util)

(def not-empty? (complement empty?))

(defn shuffle-and-take [lim coll]
  (take lim (shuffle coll)))
