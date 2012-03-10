(ns walton.core
  (:use [clojail core testers])
  (:require [clojure.java.io :as io]
            [accession.core :as redis]
            [walton.scraper :as scrape]
            [clojure.pprint :as pprint])
  (:gen-class))

;; (set! *print-length* 10)
;; (set! *print-level* 10)

(def c (redis/connection-map {}))

(def sb (sandbox secure-tester))

(defn events-with-sexps [f]
  (filter #(not (empty? (:sexp %)))
          (scrape/scrape-all-log-nodes f)))

(defn multiple-sexps? [sexps]
  (> (count sexps) 1))

(defn run-sandboxed-sexp [sexp]
  (pr-str (sb (safe-read sexp))))

(defn add-single-sexp [sexp]
  (redis/with-connection c
    (redis/hset "pass" sexp (run-sandboxed-sexp sexp))))

;; TODO: This is so wrong.
(defn scrape-and-store []
  (doseq [f scrape/local-logfiles]
    (println (.getName f))
    (doseq [event (events-with-sexps f)]
      (let [sexps (:sexp event)]
        (try
          (redis/with-connection c
            (doseq [s sexps] (add-single-sexp s)))
          (catch java.lang.Throwable t))))))

(def all-passing-sexps
  (redis/with-connection c (redis/hgetall "pass")))

(def all-passing-sexp-keys
  (redis/with-connection c (redis/hkeys "pass")))

(defn search-sexps [s]
  (filter #(re-find (re-pattern s) %) all-passing-sexps))

(defn find-examples-for [s]
  (filter #(re-find (re-pattern s) %) all-passing-sexp-keys))

(defn get-answer [s]
  (redis/with-connection c
    (redis/hget "pass" s)))

(comment
  {:Someone-in-IRC "You should make a Rich Hickey markov bot."
   :Me "Maybe"}
  (filter #(= "rhickey" (:nickname %)))
)