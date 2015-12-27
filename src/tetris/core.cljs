(ns tetris.core
  (:require [reagent.core :as reagent]
            [tetris.game :as tetris]))

(enable-console-print!)

(defonce app-state (tetris/default-state))

;; ----------------------------------------------------------------------
;; React / HTML stuff

(reagent/render-component
 [:div {:class "game"}
  [tetris/Tetris app-state]]
 (. js/document (getElementById "app")))

(def code->key
  {37 :left
   39 :right
   38 :up
   40 :down
   32 :space})

(defonce keydown-listener
  (js/document.addEventListener
   "keydown"
   (fn [e]
     (let [code (.-keyCode e)]
       (tetris/dispatch! app-state
                         [:keydown {:code code
                                    :key (code->key code)}])))))

(defonce ticker (.setInterval js/window
                              (fn [] (tetris/dispatch! app-state
                                                       [:tick 20]))
                              20))
