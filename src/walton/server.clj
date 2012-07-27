(ns walton.server
  (:use [noir.core]
        [hiccup core page form]
        [clojail.core :only (safe-read)])
  (:require [clojure.string :as s]
            [noir.server :as server]
            [walton.core :as core]
            [walton.db :as db]
            [walton.view-helpers :as helpers]
            [walton.views.common :as common]))

(server/load-views "src/walton/views")

;; (defn add-line-breaks [s]
;;   (let [s (interpose [:br] (s/split-lines s))]))

;; (defn code* [s]
;;   (let [codes (s/split-lines s)]
;;     (interpose [:br]
;;                (for [code codes]y
;;                  [:script {:type "syntaxhighlighter" :class "brush: clojure;"}
;;                   (str "<![CDATA[" (add-line-breaks s) "]]>")]))))

;; (defn code [s]
;;   (let [codes (s/split-lines s)]
;;     (interpose [:br]
;;                (for [code codes]
;;                  [:pre {:class "brush: clojure;"} code]))))

(defn search [results]
  (for [result results]
    (let [{:keys [input value out]} result]
      (if (= out "")
        (html [:dt {:id "input"} [:pre {:class "brush: clojure;"} (helpers/format-input input)]]
              [:dd {:id "value"} [:pre {:class "brush: clojure;"} value]])
        (html [:dt {:id "input"} [:pre {:class "brush: clojure;"} (helpers/format-input input)]]
              [:dd {:id "out"} [:pre {:class "brush: clojure;"} out]])))))

;; (defn search [results]
;;   (for [result results]
;;     (let [{:keys [input value out]} result]
;;       (if (= out "")
;;         (html [:dt {:id "input"} (code (helpers/format-code input))]
;;               [:dd {:id "value"} (code (helpers/format-code value))])
;;         (html [:dt {:id "input"} (code (helpers/format-code input))]
;;               [:dd {:id "out"}   (code (helpers/format-code out))])))))

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
    (form-to [:post "/search/input"]
             (search-input query)
             (submit-button "Search Input"))]
   [:div#search-value
    (form-to [:post "/search/value"]
             (search-value query)
             (submit-button "Search Value"))]
   [:div#search-out
    (form-to [:post "/search/out"]
             (search-out query)
             (submit-button "Search Output"))]))

(defpage [:post "/search/input"] {:keys [query]}
  (common/layout
   (helpers/link-to "/" "search again" {:id "search-again"})
   [:h1 (str "\"" query "\"" " examples")]
   [:dl (search (core/walton-html-input query))]))

(defpage [:post "/search/value"] {:keys [query]}
  (common/layout
   (helpers/link-to "/" "search again" {:id "search-again"})
   [:h1 (str "\"" query "\"" " examples")]
   [:dl (search (core/walton-html-value query))]))

(defpage [:post "/search/out"] {:keys [query]}
  (common/layout
   (helpers/link-to "/" "search again" {:id "search-again"})
   [:h1 (str "\"" query "\"" " examples")]
   [:dl (search (core/walton-html-out query))]))

(defn walton-server [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode, :ns 'walton})))

(comment
  (def my-server (walton-server)))
