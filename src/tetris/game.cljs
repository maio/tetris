(ns tetris.game
  (:require [reagent.core :as reagent]
            [cljs.pprint :refer [pprint]]
            [goog.string :as gstring]
            [tetris.math :refer [gen-rotated]]))

(defn default-state [& [{:keys [width height drop-speed drop-shadow?]
                         :or {width 10
                              height 20
                              drop-speed 500
                              drop-shadow? true}}]]
  (reagent/atom {;; Board dimensions
                 :width width
                 :height height
                 ;; Board holds coords of occupied cells (by pieces
                 ;; which were already dropped into place). Actually
                 ;; it's map of {[x y] => piece type (e.g. :I)}. Piece
                 ;; type defines cell color when board is being rendered.
                 :board {}
                 ;; Which key is currently pressed down?
                 :keypress? #{}
                 ;; Current piece (e.g. :I)
                 :current-piece nil
                 ;; It's rotation (i.e. 0, 90, 180, 270)
                 :rotation 0
                 ;; And current position
                 :x nil
                 :y nil
                 ;; Render shadow of a falling piece? It helps user to
                 ;; place the piece in the right place.
                 :drop-shadow? drop-shadow?
                 ;; Drop counter is incremented on each tick and if
                 ;; it's currently greater than drop speed, then piece
                 ;; is dropped by one row and counter is reset.
                 :drop-speed drop-speed
                 :drop-counter 0}))

(def nbsp (gstring/unescapeEntities "&nbsp;"))

(def piece-shapes
  {:I (gen-rotated #{[-1 0] [0 0] [1 0] [2 0]} [0.5 0.5])
   :J (gen-rotated #{[-1 0]
                     [-1 1] [0 1] [1 1]} [0 1])
   :L (gen-rotated #{,            [1 0]
                     [-1 1] [0 1] [1 1]} [0 1])
   :O (gen-rotated #{[0 0] [1 0] [0 1] [1 1]} [0.5 0.5])
   :S (gen-rotated #{,      [0 0] [1 0]
                     [-1 1] [0 1]})
   :T (gen-rotated #{[-1 0] [0 0] [1 0]
                     ,      [0 1]})
   :Z (gen-rotated #{[-1 0] [0 0]
                     ,      [0 1] [1 1]})})

(defn get-piece [kind rotation]
  (get-in piece-shapes [kind rotation]))

(def get-x first)
(def get-col first)
(def get-y second)
(def get-row second)

(defn coord+
  "Add coordinates.

   E.g. (coord+ [1 1] [2 2] [3 3]) => [6 6]"
  [& cx]
  (apply mapv + cx))

(defn min-x [kind rotation]
  (reduce min (map get-x (get-piece kind rotation))))

(defn max-x [kind rotation]
  (reduce max (map get-x (get-piece kind rotation))))

(defn max-y [kind rotation]
  (reduce max (map get-y (get-piece kind rotation))))

(defn render-shape [board kind [x y :as current-position] rotation]
  (into
   board
   (for [[sx sy] (get-piece kind rotation)]
     [(coord+ [sx sy] current-position) kind])))

(defn clear-row
  "Clear given row and drop everything above by one row."
  [board row-to-clear]
  (->> board
       ;; remove given row
       (filter
        (fn [[coords color]] (not= (get-row coords) row-to-clear)))
       ;; drop rows above by one
       (map
        (fn [[[col row] color :as original]]
          (if (< row row-to-clear)
            [[col (+ row 1)] color]
            original)))
       (into {})))

(defn find-full-rows
  [board width]
  (->> (keys board)
       ;; Let's find out number of occupied cells for each non-empty row.
       (map get-row)
       frequencies
       ;; And get those which are full.
       (filter (fn [[row number-of-occupied-cells]]
                 (= number-of-occupied-cells width)))
       (map first)))

(defn collides?
  "Return true if piece collides with board contents or it's boundaries."
  [{:keys [board width height x y current-piece rotation]}]
  (let [current (render-shape {} current-piece [x y] rotation)]
    (or
     (not-empty (filter (set (keys board)) (keys current)))
     (< (+ x (min-x current-piece rotation)) 0)
     (>= (+ x (max-x current-piece rotation)) width)
     (>= (+ y (max-y current-piece rotation)) height))))

(defn maybe-else
  "Apply function to state only if it will not result in
   collision. Othewise apply another function."
  ([state f]
   (maybe-else state f identity))
  ([state f collided-f]
   (if (collides? (f state))
     (collided-f state)
     (f state))))

(defn move-left [state]
  (update state :x dec))

(def maybe-move-left
  #(maybe-else % move-left))

(defn move-right [state]
  (update state :x inc))

(def maybe-move-right
  #(maybe-else % move-right))

(defn move-down [{:keys [drop-speed drop-counter] :as state}]
  (-> state
      (update :y inc)
      (assoc :drop-counter (max 0 (- drop-counter drop-speed)))))

(defn rotate [state]
  (update state :rotation (fn [prev] (if (= 270 prev) 0 (+ prev 90)))))

(def maybe-rotate
  #(maybe-else % rotate))

(defn can-drop?
  "Let's check whether piece can move down."
  [state]
  (not (collides? (move-down state))))

(defn force-full-drop
  "Drop piece until it hits something."
  [{:keys [current-piece] :as state}]
  (if (and current-piece
           (can-drop? state))
    (->> state
         (iterate move-down)
         (drop-while can-drop?)
         first)
    state))

(defn clear-full-rows
  "Find full rows and clear them."
  [{:keys [width] :as state}]
  (update state :board
          #(reduce (fn [board row]
                     (clear-row board row))
                   %
                   ;; Rows above should drop so make sure that we
                   ;; start at the top.
                   (sort (find-full-rows % width)))))

(defn set-next-piece
  "Reset things like position and rotation for new piece."
  [{:keys [width] :as state} piece]
  (assoc state
         :x (int
             (- (/ width 2)
                (/ (max-x piece 0) 2)))
         :y 0
         :rotation 0
         :drop-counter 0
         :current-piece piece))

(defn clear-piece [state]
  (dissoc state :current-piece :x :y :rotation :drop-counter :current-piece))

(defn generate-next-piece [state]
  (-> state (set-next-piece (rand-nth (keys piece-shapes)))))

(defn freeze-piece-in-place
  "Current piece did hit something so let's freeze it in place."
  [{:keys [current-piece x y rotation] :as state}]
  (update state :board render-shape current-piece [x y] rotation))

(defn maybe-game-over
  "Game over if piece collides with something.

   It only makes sense to call this fn right after generate-next-piece."
  [state]
  (if (collides? state)
    (assoc state :game-over true
           :current-piece nil)
    state))

(defn next-piece-or-game-over
  [{:keys [current-piece x y rotation] :as state}]
  (-> state
      freeze-piece-in-place
      clear-full-rows
      generate-next-piece
      maybe-game-over))

(defn handle-drop
  "Drop piece one row down when it's time to do so.

   If there isn't space below, try to generate next piece. If there is
   no room for it either, quit the game."
  [{:keys [drop-speed drop-counter] :as state}]
  (if (>= drop-counter drop-speed)
    (maybe-else state move-down next-piece-or-game-over)
    state))

(defn handle-keys
  [{:keys [keypress?] :as state}]
  (cond-> state
    (keypress? :left)
    (maybe-move-left)

    (keypress? :right)
    (maybe-move-right)

    (keypress? :up)
    (maybe-rotate)

    (keypress? :down)
    (maybe-else move-down next-piece-or-game-over)

    (keypress? :space)
    force-full-drop

    :cleanup
    (assoc :keypress? #{})))

(defn maybe-init
  "If there is no piece generate one."
  [{:keys [current-piece] :as state}]
  (cond-> state
    (not current-piece)
    generate-next-piece))

(defn handle-tick [state ms]
  (update state :drop-counter (partial + ms)))

;; ----------------------------------------------------------------------
;; Action handlers

(defmulti handle-action
  (fn [state type payload] type))

(defmethod handle-action :default
  [state type payload]
  state)

(defmethod handle-action :tick
  [{:keys [width height game-over] :as state} _ ms]
  ;; Game 'loop'.
  (cond-> state
    (not game-over)
    (-> (maybe-init)
        (handle-tick ms)
        (handle-keys)
        (handle-drop))))

(defmethod handle-action :keydown
  [state _ {:keys [key]}]
  (if key
    (-> state (update :keypress? conj key))
    state))

;; ----------------------------------------------------------------------
;; Dispatcher

(defn dispatch! [state [type payload]]
  (swap! state #(handle-action % type payload)))

;; ----------------------------------------------------------------------
;; UI

(defn state-sort
  "Sort that :board is always last."
  [a b]
  (if (= a :board) 1
      (if (= b :board) -1
          (compare a b))))

(defn render [{:keys [board current-piece x y rotation] :as state}]
  (-> state
      (assoc :board-to-render board)
      (update :board-to-render render-shape current-piece [x y] rotation)))

(defn render-shadow
  "Render piece shadow (i.e. where it's currently going to drop)."
  [{:keys [current-piece rotation] :as state}]
  (let [{:keys [x y]} (force-full-drop state)]
    (-> state
        (assoc :shadow-to-render {})
        (update :shadow-to-render render-shape current-piece [x y] rotation))))

(defn TetrisBoard [{:keys [drop-shadow?] :as state}]
  (let [{:keys [width height board-to-render shadow-to-render game-over]}
        (-> state render
            (cond-> drop-shadow? render-shadow))]
    [:table {:class (when game-over "game-over")}
     [:tbody
      (for [row (range height)]
        [:tr {:key row}
         (for [col (range width)]
           (let [occupied-by (get board-to-render [col row])
                 shadow-of (get shadow-to-render [col row])]
             [:td {:key col
                   :class (cond
                            occupied-by
                            (name occupied-by)

                            shadow-of
                            (str (name shadow-of) " shadow"))}
              nbsp]))])]]))

(defn Tetris [state]
  [:div
   [TetrisBoard @state]
   ;; Pretty print current state.
   [:pre (with-out-str
           (binding [*print-length* 15]
             (pprint (into (sorted-map-by state-sort) @state))))]])
