(ns walton.view-helpers
  (:use [clojail.core :only (safe-read)])
  (:require [clojure.pprint :as pp]))

(defn format-input [code]
  (with-out-str
    (pp/with-pprint-dispatch pp/code-dispatch
      (pp/pprint (safe-read code)))))

(defn link-to [path s attrs]
  [:a (merge {:href path} attrs) s])
