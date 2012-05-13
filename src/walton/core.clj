(ns walton.core
  (:use [clojail.core :only (sandbox safe-read)]
        [clojail.testers :only (secure-tester)])
  (:require [clojure.java.io :as io]
            [accession.core :as redis]
            [walton.scraper :as scrape]
            [walton.util :as util]
            [walton.clojuredocs :as docs :only (what-now?)]
            [clojure.pprint :as pprint])
  (:gen-class))

;; (set! *print-length* 10)
;; (set! *print-level* 10)

(def c (redis/connection-map {}))

(def sb (sandbox secure-tester))

(defn has-sexp?
  "Checks that an s-expression exists on a map"
  [m]
  (util/not-empty? (:sexp m)))

(defn events-with-sexps [f]
  (filter has-sexp? (scrape/scrape-all-log-nodes f)))

(defn multiple-sexps?
  "Returns true or false if there is more than 1
  s-expression for a given log line."
  [sexps]
  (> (count sexps) 1))

(defn run-sandboxed-sexp
  "Safely read a sexp and run it in the sandbox."
  [sexp]
  (pr-str (sb (safe-read sexp))))

(defn add-single-sexp
  "Add a single sexp to the redis database
  where the key is the input expression, and the
  value is the output of running the sandboxed
  expression."
  [sexp]
  (redis/hset "pass" sexp (run-sandboxed-sexp sexp)))

(defn scrape-and-store
  "Scrape all logfiles, and for each log line
  that contains one or more sexps, run each of them
  in the sandbox. If it succeeds, add it to the redis
  database."
  []
  (doseq [f scrape/local-logfiles]
    (println (.getName f))
    (doseq [event (events-with-sexps f)]
      (let [sexps (:sexp event)]
        (try
          (doseq [s sexps]
            (redis/with-connection c (add-single-sexp s)))
          (catch java.lang.Throwable t))))))

(def all-passing-sexps
  (redis/with-connection c (redis/hgetall "pass")))

(def all-passing-sexp-groups
  (partition 2 (redis/with-connection c
                 (redis/hgetall "pass"))))

(defn find-examples-by-input
  "Find examples where the input matches and
  return the matches.

  user> (find-examples-by-input \"input\")
  => ((\"(input sexp)\", \"output\"), ...)"
  [s]
  (filter #(re-find (re-pattern s) (first %))
          all-passing-sexp-groups))

(defn find-examples-where-val-eq
  "Find examples where the output is exactly the
  string s.

  user> (find-examples-where-val-eq \"foo\")
  => ((\"(str \"foo\")\", \"foo\"), ...)"
  [s]
  (filter #(= s (second %))
          all-passing-sexp-groups))

(defn find-examples-where-val-sort-of
  "Find examples where the output is sort of
  the string s

  user> (find-examples-where-val-sort-of \"3\")
  => ((\"(str \"3333\")\", \"3333\"), ...)"
  [s]
  (filter #(re-find (re-pattern s) (second %))
          all-passing-sexp-groups))

;; (defn print-examples-for [s]
;;   (doseq [sexp (shuffle (find-examples-for s))]
;;     (println sexp)))

(defn starts-with-core-fn? [s]
  (let [core-fns (map (comp str first) (ns-publics 'clojure.core))]
    (some #(= % ((comp str first) (safe-read s)))
          core-fns)))

(defn walton
  ([s] (doseq [sexp (map first (find-examples-by-input s))]
         (println sexp)))
  ([s lim] (doseq [sexp (take lim (shuffle (map first (find-examples-by-input s))))]
             (println sexp))))

(defn walton-html
  ([s] (find-examples-by-input s))
  ([s lim] (take lim (shuffle (find-examples-by-input s)))))

(defn notlaw-html
  ([s] (find-examples-where-val-sort-of s))
  ([s lim] (take lim (shuffle (find-examples-where-val-sort-of s)))))

(defn notlaw
  ([s] (doseq [v (find-examples-where-val-eq s)]
         (println (first v))))
  ([s lim] (doseq [v (take lim (shuffle (find-examples-where-val-eq s)))]
             (println (first v)))))

(defn notlaw-plus [s lim]
  (doseq [v (take lim (shuffle (find-examples-where-val-sort-of s)))]
    (println (first v))))
