(ns walton.server
  (:use [noir.core]
        [hiccup core page form]
        [clojail.core :only (safe-read)])
  (:require [clojure.pprint :as pp]
            [clojure.string :as s]
            [noir.server :as server]
            [walton.core :as core]
            [walton.views.common :as common]))

(server/load-views "src/walton/views")

(defn format-code [code]
  (pp/with-pprint-dispatch pp/code-dispatch code))

(defn link-to [path s attrs]
  [:a (merge {:href path} attrs) s])

(defn search [results]
  (for [result results]
    (let [{:keys [input value out]} result]
      (if-not (and (= value "nil"))
        (html [:dt {:id "input"} [:pre {:class "brush: clojure;"} (format-code input)]]
              [:dd {:id "value"} [:pre {:class "brush: clojure;"} (format-code value)]])
        (html [:dt {:id "input"} [:pre {:class "brush: clojure;"} (format-code input)]]
              [:dd {:id "out"} [:pre {:class "brush: clojure;"} (format-code out)]])))))

(defpartial search-input [{:keys [query]}]
  (label "query" "Search by Input: ")
  (text-field "query" query))

(defpartial search-value [{:keys [query]}]
  (label "query" "Search by Value: ")
  (text-field "query" query))

(defpartial search-out [{:keys [query]}]
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
      (submit-button "Search Value"))]
   [:div#search-out
    (form-to
        [:post "/search/out"]
        (search-out query)
      (submit-button "Search Output"))]))

(defpage [:post "/search/input"] {:keys [query]}
  (common/layout
   (link-to "/" "search again" {:id "search-again"})
   [:h1 (str "\"" query "\"" " examples")]
   [:dl (search (core/walton-html-input query))]))

(defpage [:post "/search/value"] {:keys [query]}
  (common/layout
   (link-to "/" "search again" {:id "search-again"})
   [:h1 (str "\"" query "\"" " examples")]
   [:dl (search (core/walton-html-value query))]))

(defpage [:post "/search/out"] {:keys [query]}
  (common/layout
   (link-to "/" "search again" {:id "search-again"})
   [:h1 (str "\"" query "\"" " examples")]
   [:dl (search (core/walton-html-out query))]))

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode, :ns 'walton})))

(comment
  (def my-server (-main)))
