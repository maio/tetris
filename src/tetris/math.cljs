(ns tetris.math)

(def rotation-m
  ;; [[cos -sin] [sin cos]]
  {0 [[1 0] [0 1]]
   90 [[0 -1] [1 0]]
   180 [[-1 0] [0 -1]]
   270 [[0 1] [-1 0]]})

(defn rot [[x y] rotation]
  (let [[[r11 r12] [r21 r22]] (rotation-m rotation)]
    [(+ (* r11 x) (* r12 y))
     (+ (* r21 x) (* r22 y))]))

(defn rot-p
  "Rotate point around given pivot."
  [[x y] rotation [px py]]
  (let [[nx ny] (rot [(- x px) (- y py)] rotation)]
    [(+ nx px) (+ ny py)]))

(defn gen-rotated [shape & [pivot]]
  (let [pivot (or pivot [0 0])]
    (into
     {}
     (for [r [0 90 180 270]]
       [r (set (map #(rot-p % r pivot) shape))]))))

