(defproject functional-programming-lab4 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.10.773"]
                 [org.clojure/core.async  "0.4.500"]
                 [reagent "0.10.0"]
                 [cljs-ajax/cljs-ajax "0.8.0"]
                 [markdown-clj "1.12.0"]]

  :plugins [[lein-figwheel "0.5.20"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]

                :figwheel {:on-jsload "front/on-js-reload"
                           :open-urls ["http://localhost:3449/index.html"]}

                :compiler {:main front
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/front.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}

               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/front.js"
                           :main front
                           :optimizations :advanced
                           :pretty-print false}}]}

  :profiles {:dev {:dependencies [[binaryage/devtools "1.0.0"]
                                  [figwheel-sidecar "0.5.20"]]
                   :source-paths ["src" "dev"]
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
