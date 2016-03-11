(ns tetris.cards-helpers
  (:require [tetris.game :as tetris]))

(defn modify-state! [state fns]
  (swap! state (apply comp (reverse fns)))
  state)

(defn state-with [defaults fns]
  (let [state (tetris/default-state defaults)]
    (modify-state! state fns)
    state))

(defn TetrisBoards
  ([state]
   [TetrisBoards state []])
  ([state fns]
   [TetrisBoards state fns {}])
  ([state fns {:keys [show-score?] :as opts}]
   [:div {:style {:display "flex" :margin-bottom "7px"}}
    (doall
     (for [[idx fn] (map-indexed (fn [idx itm] [idx itm])
                                 (concat [identity] (or fns [])))]
       (let [s (fn @state)]
         [:div {:key idx
                :style {:margin-right "10px"}}
          [tetris/TetrisBoard s]
          (when show-score? [:div (:score s)])])))]))

(defn FullTickButton [state]
  [:button {:on-click #(tetris/dispatch! state [:tick (@state :drop-speed)])}
   "v"])

(defn RotateButton [state]
  [:button {:on-click #(modify-state! state [tetris/maybe-rotate])}
   "@"])

(defn MoveLeftButton [state]
  [:button {:on-click #(modify-state! state [tetris/maybe-move-left])}
   "<"])

(defn MoveRightButton [state]
  [:button {:on-click #(modify-state! state [tetris/maybe-move-right])}
   ">"])
