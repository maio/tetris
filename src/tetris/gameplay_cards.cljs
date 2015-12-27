(ns tetris.gameplay-cards
  (:require [devcards.core]
            [tetris.cards-helpers :refer [TetrisBoards state-with RotateButton
                                          MoveLeftButton MoveRightButton
                                          FullTickButton]]
            [tetris.game :as tetris])
  (:require-macros [devcards.core :refer [defcard-rg]]))

(defn drop-while-can [{:keys [drop-speed] :as state}]
  (->> state
       (iterate #(-> %
                     (tetris/handle-tick drop-speed)
                     (tetris/handle-drop)))
       ;; We don't want to iterate forever if there is a bug in
       ;; collision detection so let's only iterate 10 times max.
       (take 10)
       (drop-while tetris/can-drop?)
       first))

(defcard-rg collision-with-bottom
  "Falling piece should stop at the bottom."
  (fn [state _]
    [:div
     [TetrisBoards state drop-while-can]
     [MoveLeftButton state]
     [RotateButton state]
     [MoveRightButton state]])

  (state-with {:width 5 :height 5}
              [#(tetris/set-next-piece % :T)]))

(defcard-rg collision-with-other-pieces
  "Falling piece should stop when it collides with other piece."
  (fn [state _]
    [:div
     [TetrisBoards state
      drop-while-can
      (comp drop-while-can tetris/maybe-move-right)
      (comp drop-while-can tetris/maybe-move-right tetris/maybe-move-right)]
     [MoveLeftButton state]
     [RotateButton state]
     [MoveRightButton state]])

  (state-with {:width 5 :height 5}
              [#(tetris/set-next-piece % :Z)
               tetris/move-left
               drop-while-can
               tetris/freeze-piece-in-place
               #(tetris/set-next-piece % :T)
               tetris/move-left]))

(defcard-rg clear-row
  "Row should be cleared when it's full."
  (fn [state _]
    [:div
     [TetrisBoards state
      drop-while-can
      (comp tetris/clear-full-rows
            tetris/clear-piece
            tetris/freeze-piece-in-place
            drop-while-can)]
     [MoveLeftButton state]
     [RotateButton state]
     [MoveRightButton state]])

  (state-with {:width 5 :height 5}
              [#(tetris/set-next-piece % :Z)
               tetris/move-left
               drop-while-can
               tetris/freeze-piece-in-place
               #(tetris/set-next-piece % :T)
               tetris/move-right]))

(defcard-rg full-gameplay
  (fn [state _]
    [:div
     [TetrisBoards state]
     [:div {:style {:display "flex" :align-items "center"}}
      [MoveLeftButton state]
      [:div {:style {:display "flex" :flex-direction "column"}}
       [RotateButton state]
       [FullTickButton state]]
      [MoveRightButton state]]])

  (state-with {:width 5 :height 10} [tetris/maybe-init])
  {:inspect-data true :history true})
