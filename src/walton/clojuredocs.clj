(ns walton.clojuredocs
  (:require [yokogiri.core :as y]
            [clojure.string :as s]))

(def c (y/make-client))

(def core-page
  (y/get-page c "http://clojuredocs.org/clojure_core/clojure.core"))

(def core-fn-links
  (subvec
   (vec (map #(str "http://clojuredocs.org"
                   (:href (y/attr-map %)))
             (y/css core-page "span>a")))
   27 614))

(def clean-fn-links
  (filter #(not= (last (seq %)) \') core-fn-links))

(def all-example-maps (atom []))

(defn get-example-count [url]
  (try
    (Integer/parseInt
     (y/node-text
      (first (y/css (y/get-page c url) "span#examples_count"))))
    (catch Exception e (str "Caught exception: " e))))

(defn build-example-map [url]
 (swap! all-example-maps conj {:count (get-example-count url),
                               :fn-name (last (s/split url #"/"))}))

(comment (.decode (java.net.URI. uri)))

(defn get-all-example-maps []
  (doseq [link clean-fn-links]
    (build-example-map link)))

(defn what-now? []
  (doseq [l (filter #(= 0 (:count %)) @all-example-maps)]
    (println (:fn-name l))))

;; Utility
(def zero-examples
  (filter #(zero? (:count %)) all-example-maps))



