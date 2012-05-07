(defproject walton "0.5.0"
  :description "Find the crystal skull in Clojure. Consult the oracle."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [paddleguru/enlive "1.2.0-alpha1"]
                 [ibdknox/clojail "0.5.2"]
                 [accession "0.1.1"]
                 [hiccup "1.0.0-SNAPSHOT"]
                 [clj-http "0.3.3"]
                 [clojure-cheatsheets "1.0"]
                 [yokogiri "0.0.2"]]
  :dev-dependencies [[clj-ns-browser "1.1.0"]
                     [midje "1.3.2-alpha1"]]
  :main walton.core
  :jvm-opts ["-Xmx2G" "-Xms1G" "-server"])
