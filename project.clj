(defproject spec-tac-joe "0.1.0-SNAPSHOT"
  :description "Spec-driven tic tac toe"
  :url "https://github.com/djtango/spec-tac-joe"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [midje "1.9.0-alpha6"]
                 [orchestra "2017.11.12-1"]]
  :plugins [[lein-midje "3.1.1"]]
  :repl-options {:init-ns railways.core
                 :init (do (require '[midje.repl :as midje.repl])
                           (midje.repl/autotest))})
