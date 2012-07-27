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
  (conj secure-tester
        'print-method
        (p "java.util.regex.Pattern")))

(def sb (sandbox walton-tester :transform pr-str))

(defn has-sexp?
  "Checks that an s-expression exists on a map"
  [m]
  (util/not-empty? (:sexp m)))

(defn get-lines-with-sexps
  "Scrape lines from a logfile which have an s-expression."
  [f]
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

(defn fns-for-ns
  "Returns a sequence of strings of the public functions in a
  namespace."
  [nspace]
  (map (comp str first) (ns-publics nspace)))

(def clojure-core-fns (fns-for-ns 'clojure.core))

(defn starts-with-core-fn? [s]
  (some #(= % ((comp str first) (safe-read s))) clojure-core-fns))

(defn contains-readable-core-fn? [s]
  (let [safe-form-set (set (map str (flatten (safe-read s))))]
    (some safe-form-set clojure-core-fns)))

(defn shuffle-and-take [lim coll]
  (take lim (shuffle coll)))

(defn print-sexp-map [sexp-map]
  (let [{:keys [input value out]} sexp-map]
    (println "Input:" input)
    (println "Value:" value)
    (println "Out:" out)
    (println "---")))

(defn print-sexp-maps [query-fn query-str & [lim]]
  (let [query-op (query-fn query-str)
        query-results (if lim (shuffle-and-take lim query-op) query-op)]
    (doseq [sexp-map query-results]
      (print-sexp-map sexp-map))))

(defn resolve-query-fn [kw]
  ((comp resolve symbol str) (str "db/exprs-where-" (name kw))))

(defn walton [kw query-str & [lim]]
  (print-sexp-maps (resolve-query-fn kw) query-str lim))

(defn walton-html [kw query-str]
  (let [query-op (resolve-query-fn kw)]
    (query-op query-str)))

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
