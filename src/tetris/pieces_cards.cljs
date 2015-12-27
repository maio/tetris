(ns tetris.pieces-cards
  (:require [devcards.core]
            [tetris.cards-helpers :refer [TetrisBoards state-with RotateButton]]
            [tetris.game :as tetris])
  (:require-macros [devcards.core :refer [defcard-rg]]))

(defn Piece [state]
  [TetrisBoards state
   tetris/rotate
   (comp tetris/rotate tetris/rotate)
   (comp tetris/rotate tetris/rotate tetris/rotate)])

(defcard-rg I-piece
  (fn [state _]
    [:div
     [Piece state]
     [RotateButton state]])

  (state-with {:width 4 :height 4}
              [#(tetris/set-next-piece % :I) tetris/move-down]))

(defcard-rg J-piece
  (fn [state _]
    [:div
     [Piece state]
     [RotateButton state]])

  (state-with {:width 3 :height 3} [#(tetris/set-next-piece % :J)]))

(defcard-rg L-piece
  (fn [state _]
    [:div
     [Piece state]
     [RotateButton state]])

  (state-with {:width 3 :height 3} [#(tetris/set-next-piece % :L)]))

(defcard-rg O-piece
  (fn [state _]
    [:div
     [Piece state]
     [RotateButton state]])

  (state-with {:width 2 :height 2} [#(tetris/set-next-piece % :O)]))

(defcard-rg S-piece
  (fn [state _]
    [:div
     [Piece state]
     [RotateButton state]])

  (state-with {:width 3 :height 3}
              [#(tetris/set-next-piece % :S) tetris/move-down]))

(defcard-rg T-piece
  (fn [state _]
    [:div
     [Piece state]
     [RotateButton state]])

  (state-with {:width 3 :height 3}
              [#(tetris/set-next-piece % :T) tetris/move-down]))

(defcard-rg Z-piece
  (fn [state _]
    [:div
     [Piece state]
     [RotateButton state]])

  (state-with {:width 3 :height 3}
              [#(tetris/set-next-piece % :Z) tetris/move-down]))
