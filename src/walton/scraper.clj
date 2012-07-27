(ns walton.scraper
  (:require [net.cgrand.enlive-html :as e]
            [yokogiri.core :as y]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.set :as s]))

(def dates-url "http://clojure-log.n01se.net/date/")

(defn remote-logfiles []
  (let [html-re #"\>(.*\.html)\<"]
    (map second (re-seq html-re (apply str (slurp dates-url))))))

(defn local-logfiles []
  (filter #(re-find #".*\.html$" (str %))
          (file-seq (io/file "logs"))))

(defn missing-local-logfiles []
  (s/difference
   (set (butlast (sort (remote-logfiles))))
   (set (map #(.getName %) (local-logfiles)))))

(defn missing-logfiles? []
  (when-not (empty? (missing-local-logfiles))
    true))

(defn get-missing-logfiles []
  (if (missing-logfiles?)
    (doseq [log missing-local-logfiles]
      (println (str "Downloading " log "..."))
      (let [log-data (slurp (str dates-url log))]
        (spit (io/file "logs" log) log-data)))))

(defn extract-expressions
  "Extracts sexps."
  [string]
  (second
   (reduce (fn [[exp exps state cnt] c]
             (cond
              (= state :escape)
              [(.append exp c) exps :string cnt]
              (= state :string) (cond
                                 (= c \")
                                 [(.append exp c) exps :code cnt]
                                 (= c \\)
                                 [(.append exp c) exps :escape cnt]
                                 (= c \\)
                                 [(.append exp c) exps :escape cnt]
                                 :else
                                 [(.append exp c) exps :string cnt])
              (and (= cnt 1) (= c \)))
              [(java.lang.StringBuilder.) (cons (str (.append exp c)) exps) :text 0]
              (= c \()
              [(.append exp c) exps :code (inc cnt)]
              (and (> cnt 1) (= c \)))
              [(.append exp c) exps :code (dec cnt)]
              (and (> cnt 0) (= c \"))
              [(.append exp c) exps :string cnt]
              (> cnt 0)
              [(.append exp c) exps :code cnt]
              :else [exp exps state cnt]))
           [(java.lang.StringBuilder.) '() :text 0]
           string)))

(defn get-lines [f]
  (e/select (e/html-resource f) [:p]))

(defn text-for [node kw]
  (first (e/texts (e/select node [kw]))))

(defn trim-nickname [s]
  (if s (string/replace s #": " "")))

(defn trim-content [s]
  (string/triml (string/trim-newline s)))

(defn extract-sexps [s]
  (let [matches (extract-expressions s)]
    (if-not (empty? matches)
      matches)))

(defn scrape-log-line-without-sexp [node]
  (let [nickname (trim-nickname (text-for node :b))
        content  (trim-content  (last (:content node)))]
    {:nickname nickname
     :content content}))

(defn scrape-log-line [node]
  (let [nickname  (trim-nickname (text-for node :b))
        timestamp (text-for node :a)
        content   (trim-content (last (:content node)))]
    {:nickname nickname
     :timestamp timestamp
     :content content
     :sexp (extract-sexps content)}))

(defn fix-empty-nicknames [smaps]
  (rest
   (reductions
    (fn [{prev :nickname} next]
      (update-in next [:nickname] #(or % prev)))
    {}
    smaps)))

(defn scrape-all-log-lines [f]
  (fix-empty-nicknames
   (map scrape-log-line (get-lines f))))

(defn scrape-all-log-lines-without-sexps [f]
  (fix-empty-nicknames
   (map scrape-log-line-without-sexp (get-lines f))))

(comment
  (set! *print-level* 1)
  (set! *print-length* 1)
  (def lf (last local-logfiles))
  (def node (first (e/select (e/html-resource lf) [:p])))
  (e/select (e/html-resource lf) [:p :> [e/any-node (e/but-node [:a e/first-child])]]))
