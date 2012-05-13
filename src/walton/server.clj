(ns walton.server
  (:use [noir.core]
        [hiccup core page form]
        [clojail.core :only (safe-read)])
  (:require [clojure.pprint :as pp]
            [noir.server :as server]
            [walton.core :as core]
            [walton.views.common :as common]))

(server/load-views "src/walton/views")

(defn format-code [code]
  (pp/with-pprint-dispatch pp/code-dispatch
    (safe-read code)))

(defn search [results]
  (for [result results]
    (let [[k v] result]
      (html [:dt [:pre {:class "brush: clojure;"} k]]
            [:dd [:pre v]]))))

(defpartial search-input [{:keys [query]}]
  (label "query" "Search by Input: ")
  (text-field "query" query))

(defpartial search-value [{:keys [query]}]
  (label "query" "Search by Output: ")
  (text-field "query" query))

(defpage "/" {:as query}
  (common/layout
   [:h1 "getclojure"]
   [:div#search-input
    (form-to
     [:post "/search/input"]
     (search-input query)
     (submit-button "Search Input"))]
   [:div#search-value
    (form-to
     [:post "/search/value"]
     (search-value query)
     (submit-button "Search Value"))]))

(defpage [:post "/search/input"] {:keys [query]}
  (common/layout
   [:h1 (str "\"" query "\"" " examples")]
   [:dl (search (core/walton-html query))]))

(defpage [:post "/search/value"] {:keys [query]}
  (common/layout
   [:h1 (str "\"" query "\"" " examples")]
   [:dl (search (core/notlaw-html query))]))

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode, :ns 'walton})))

(comment
  (def my-server (-main)))
