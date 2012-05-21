(ns walton.core
  (:use [clojure.stacktrace :only (root-cause)]
        [clojail.core :only (sandbox safe-read)]
        [clojail.testers :only (secure-tester p)])
  (:require [clojure.java.io :as io]
            [walton.db :as db]
            [walton.scraper :as scrape]
            [walton.util :as util])
  (:import java.io.StringWriter
           java.util.concurrent.TimeoutException)
  (:gen-class))

;; (set! *print-length* 10)
;; (set! *print-level* 10)

(def ^{:private true} walton-tester
  (conj secure-tester 'print-method (p "java.util.regex.Pattern")))

(def sb (sandbox walton-tester
                 :transform pr-str))

(defn has-sexp?
  "Checks that an s-expression exists on a map"
  [m]
  (util/not-empty? (:sexp m)))

(defn get-lines-with-sexps [f]
  (filter has-sexp? (scrape/scrape-all-log-lines f)))

(defn multiple-sexps?
  "Returns true or false if there is more than 1 s-expression for a
  given log line."  [sexps]
  (> (count sexps) 1))

(defn run-sandboxed-sexp
  "Safely read a sexp and run it in the sandbox capturing the
  value. Captures *out* in case the expression being evaluated prints
  to *out*. Returns a map of the input, value, and out if there is any."
  [sexp]
  (with-open [writer (StringWriter.)]
    (let [bindings {#'*out* writer}
          value (sb (safe-read sexp) bindings)
          out (str writer)]
      {:input sexp :value value :out out})))

(defn init-walton!
  "Scrape all logfiles, and for each log line that contains one or
  more sexps, run each of them in the sandbox. If it succeeds, add it
  to the database."
  []
  (doseq [f scrape/local-logfiles]
    (println (.getName f))
    (doseq [line (get-lines-with-sexps f)]
      (let [sexps (:sexp line)]
        (try (doseq [s sexps]
               (db/insert-expression
                (run-sandboxed-sexp s)))
             (catch TimeoutException _ "Execution timed out!")
             (catch Throwable t))))))

;; (def all-passing-sexps
;;   (redis/with-connection c (redis/hgetall "pass")))

;; (def all-passing-sexp-groups
;;   (partition 2 (redis/with-connection c
;;                  (redis/hgetall "pass"))))

;; (defn find-examples-by-input
;;   "Find examples where the input matches and
;;   return the matches.

;;   user> (find-examples-by-input \"input\")
;;   => ((\"(input sexp)\", \"output\"), ...)"
;;   [s]
;;   (filter #(re-find (re-pattern s) (first %))
;;           all-passing-sexp-groups))

;; (defn find-examples-where-val-eq
;;   "Find examples where the output is exactly the
;;   string s.

;;   user> (find-examples-where-val-eq \"foo\")
;;   => ((\"(str \"foo\")\", \"foo\"), ...)"
;;   [s]
;;   (filter #(= s (second %))
;;           all-passing-sexp-groups))

;; (defn find-examples-where-val-sort-of
;;   "Find examples where the output is sort of
;;   the string s

;;   user> (find-examples-where-val-sort-of \"3\")
;;   => ((\"(str \"3333\")\", \"3333\"), ...)"
;;   [s]
;;   (filter #(re-find (re-pattern s) (second %))
;;           all-passing-sexp-groups))

;; (defn starts-with-core-fn? [s]
;;   (let [core-fns (map (comp str first) (ns-publics 'clojure.core))]
;;     (some #(= % ((comp str first) (safe-read s)))
;;           core-fns)))

;; (defn walton
;;   ([s] (doseq [sexp (map first (db/find-examples-by-input s))]
;;          (println sexp)))
;;   ([s lim] (doseq [sexp (take lim (shuffle (map first (db/find-examples-by-input s))))]
;;              (println sexp))))

;; (defn walton-html
;;   ([s] (db/find-examples-by-input s))
;;   ([s lim] (take lim (shuffle (db/find-examples-by-input s)))))

;; (defn notlaw
;;   ([s] (doseq [v (find-examples-where-val-eq s)]
;;          (println (first v))))
;;   ([s lim] (doseq [v (take lim (shuffle (find-examples-where-val-eq s)))]
;;              (println (first v)))))

;; (defn notlaw-html
;;   ([s] (find-examples-where-val-sort-of s))
;;   ([s lim] (take lim (shuffle (find-examples-where-val-sort-of s)))))

;; (defn notlaw-plus [s lim]
;;   (doseq [v (take lim (shuffle (find-examples-where-val-sort-of s)))]
;;     (println (first v))))

(comment
  (def background-init-walton (.start (Thread. (fn [] (dorun (init-walton!))))))

  (defn run-single-file-through-sandbox [f]
    (doseq [event (get-lines-with-sexps f)]
      (let [sexps (:sexp event)]
        (try (doseq [s sexps]
               (db/insert-expression
                (run-sandboxed-sexp s)))
             (catch TimeoutException _ "Execution timed out!")
             (catch Throwable t "Expression did not pass the sniff test!"))))))
