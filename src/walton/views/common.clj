(ns walton.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page :only [include-css include-js html5]]))

(defpartial layout [& content]
  (html5
   [:head
    [:title "getclojure"]
    (include-css "/css/reset.css")
    (include-css "/css/global.css")
    (include-css "/css/shCoreDefault.css")
    (include-css "/css/shClojureExtra.css")
    (include-js "/js/shCore.js")
    (include-js "/js/shBrushClojure.js")
    [:script {:type "text/javascript"} "SyntaxHighlighter.defaults['gutter'] = false;"]
    [:script {:type "text/javascript"} "SyntaxHighlighter.defaults['collapse'] = false;"]
    [:script {:type "text/javascript"} "SyntaxHighlighter.defaults['toolbar'] = false;"]
    [:script {:type "text/javascript"} "SyntaxHighlighter.all();"]]
   [:body
    [:div#wrapper
     content]]))