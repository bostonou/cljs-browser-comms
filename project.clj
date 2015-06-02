(defproject com.bostonou/cljs-browser-comms "0.1.0-SNAPSHOT"
  :description "Lib for communicating between browser windows/tabs."
  :url "https://github.com/bostonou/cljs-browser-comms"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-beta3"]
                 [org.clojure/clojurescript "0.0-3269"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [prismatic/schema "0.3.7"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.1"]]

  :plugins [[lein-cljsbuild "1.0.5"]]

  :source-paths ["src" "target/classes"]

  :clean-targets ["out" "out-adv"]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src"]
              :compiler {
                :main cljs.browser-comms.core
                :output-to "out/cljs/browser_comms.js"
                :output-dir "out"
                :optimizations :none
                :cache-analysis true
                :source-map true}}
             {:id "release"
              :source-paths ["src"]
              :compiler {
                :main cljs.browser-comms.core
                :output-to "out-adv/cljs/browser_comms.min.js"
                :output-dir "out-adv"
                :optimizations :advanced
                :pretty-print false}}]})
