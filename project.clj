(defproject walton "0.5.0"
  :description "Find the crystal skull in Clojure. Consult the oracle."
  :url "http://github.com/devn/indiana"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [paddleguru/enlive "1.2.0-alpha1"]
                 [yokogiri "0.0.2"]
                 [ibdknox/clojail "0.5.2"]
                 [noir "1.3.0-beta6"]
                 [korma "0.3.0-beta10"]
                 [postgresql "9.1-901-1.jdbc4"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [clj-http "0.3.3"]
                 [org.clojure/data.json "0.1.2"]]
  :dev-dependencies [[midje "1.4.0"]]
  :main walton.core
  :jvm-opts ["-Xmx4G" "-Xms1G" "-server"])
