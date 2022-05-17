(defproject com.andreyfadeev/wiremock-testing "1.0.0"
  :description "How to use Wiremock in Clojure tests"
  :url "http://andreyfadeev.com/how-to-use-wiremock-in-clojure-tests"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [freeport "1.0.0"]
                 [http-kit "2.5.3"]
                 [com.github.tomakehurst/wiremock "2.27.2"]
                 [metosin/jsonista "0.3.5"]]
  :plugins [[lein-cljfmt "0.8.0"]])
