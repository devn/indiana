(ns walton.html
  (:use [hiccup.core]
        [walton.core])
  (:require [accession.core :as redis]))

(def page
  (html [:html
         [:head [:title "Walton: Passing"]]
         [:body
          [:ul
           (for [passing-sexp-key all-passing-sexps]
             [:li
              [:h3 passing-sexp-key]
              "=>"
              [:p (redis/with-connection c
                    (redis/hget "pass" passing-sexp-key))]])]]]))

(defn gen-page-for-match [search-term]
  (html
   [:html
    [:head [:title search-term]]
    [:body
     [:h1 (str "Results for: " search-term)]
     [:dl
      (for [sexp (search-sexps search-term)]
        [:dd sexp]
        [:dt (redis/with-connection c
               (redis/hget "pass" sexp))])]]]))