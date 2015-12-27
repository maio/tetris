(defproject tetris "0.1.0-SNAPSHOT"
  :description "Tetris Devcards experiment"
  :url "https://github.com/maio/tetris"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [devcards "0.2.1-6"]
                 [reagent "0.5.1"]]

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-figwheel "0.5.0-6"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]

                :figwheel {:on-jsload "tetris.core/on-js-reload"}

                :compiler {:main tetris.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/tetris.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}

               {:id "cards"
                :source-paths ["src"]

                :figwheel {:devcards true}

                :compiler {:main tetris.cards
                           :optimizations :none
                           :asset-path "js/compiled/out-cards"
                           :output-to "resources/public/js/compiled/cards.js"
                           :output-dir "resources/public/js/compiled/out-cards"
                           :source-map-timestamp true}}

               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/tetris.js"
                           :main tetris.core
                           :optimizations :advanced
                           :pretty-print false}}

               {:id "min-cards"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/cards.js"
                           :main tetris.cards
                           :devcards true
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {:server-port 3450
             ;; watch and update CSS
             :css-dirs ["resources/public/css"]})
