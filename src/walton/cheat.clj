(ns walton.cheat
  (:require [clojure-cheatsheets.generator :as cs]
            [net.cgrand.enlive-html :as e]
            [clojure.java.io :as io]
            [clj-http.client :as client]))

(def cheatsheet-url "http://clojure.org/cheatsheet")

(defn get-latest-cheatsheet []
  (spit
   (io/file "cheatsheet.html")
   (:body (client/get cheatsheet-url))))

(def cheatsheet-html
  (e/select
   (e/html-resource
    (io/file "cheatsheet.html"))
   [:div#content_view]))

(defn extract-columns []
  (e/select cheatsheet-html [:div.column]))

(defn extract-section [column]
  (e/select column [:div.section]))

(comment
 (def a-section
   (first (extract-section (first (extract-columns))))))

(defn extract-header [section]
  (first (e/select section [:h2 e/text])))

(defn extract-subsection-header [section]
  (first (e/select section [:h3 e/text])))

(defn subsection? [section]
  (empty? (extract-header section)))

(defn extract-section-tables [section]
  (e/select section [:table]))

(defn extract-table-header [table]
  (e/select table [:tr :td]))

(defn section->hash [section]
  (let [header (extract-header section)
        ]))

(defn -main []
  (get-latest-cheatsheet))