(ns walton.core
  (:use [clojail.core]
        [clojail.testers])
  (:require [clojure.java.io :as io]
            [accession.core :as redis]
            [walton.scraper :as scrape]
            [walton.clojuredocs :as docs :only (what-now?)]
            [clojure.pprint :as pprint])
  (:gen-class))

;; (set! *print-length* 10)
;; (set! *print-level* 10)

(def c (redis/connection-map {}))

(def sb (sandbox secure-tester))

(defn not-empty? [x]
  (not (empty? x)))

(defn has-sexp?
  "Checks that an s-expression exists on a map"
  [m]
  (not-empty? (:sexp m)))

(defn events-with-sexps [f]
  (filter has-sexp? (scrape/scrape-all-log-nodes f)))

(defn multiple-sexps? [sexps]
  (> (count sexps) 1))

(defn run-sandboxed-sexp [sexp]
  (pr-str (sb (safe-read sexp))))

(defn add-single-sexp [sexp]
  (redis/hset "pass" sexp (run-sandboxed-sexp sexp)))

;; TODO: This is so wrong.
(defn scrape-and-store []
  (doseq [f scrape/local-logfiles]
    (println (.getName f))
    (doseq [event (events-with-sexps f)]
      (let [sexps (:sexp event)]
        (try
          (doseq [s sexps]
            (redis/with-connection c (add-single-sexp s)))
          (catch java.lang.Throwable t))))))

(def all-passing-sexp-keys
  (redis/with-connection c (redis/hkeys "pass")))

(def all-passing-sexps
  (redis/with-connection c (redis/hgetall "pass")))

(defn search-sexps [s]
  (filter #(re-find (re-pattern s) %) all-passing-sexps))

(defn find-examples-for [s]
  (filter #(re-find (re-pattern s) %) all-passing-sexp-keys))

(defn find-examples-where-val-eq [s]
  (filter #(= s (second %)) (partition 2 all-passing-sexps)))

(defn find-examples-where-val-sort-of [s]
  (filter #(re-find (re-pattern s) (second %))
          (partition 2 all-passing-sexps)))

(defn print-examples-for [s]
  (doseq [sexp (shuffle (find-examples-for s))]
    (println sexp)))

(defn walton
  ([s]
     (doseq [sexp (find-examples-for s)]
       (println sexp)))
  ([s lim]
     (doseq [sexp (take lim (shuffle (find-examples-for s)))]
       (println sexp))))

(defn notlaw
  ([s]
     (doseq [v (find-examples-where-val-eq s)]
       (println (first v))))
  ([s lim]
     (doseq [v (take lim (shuffle (find-examples-where-val-eq s)))]
       (println (first v)))))

(defn notlaw-plus [s lim]
  (doseq [v (take lim (shuffle (find-examples-where-val-sort-of s)))]
    (println (first v))))

(defn get-answer [s]
  (redis/with-connection c
    (redis/hget "pass" s)))
