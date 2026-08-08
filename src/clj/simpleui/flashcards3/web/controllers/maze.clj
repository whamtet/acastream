(ns simpleui.flashcards3.web.controllers.maze
  (:require
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]))

(def m 9)
(def n 9)
(def m2 (- m 4))
(def n2 (- n 4))

(defn- place-words [words]
  (let [words (filter #(< 1 (count %) n2) words)
        step (->> words count (/ m2) long (max 3))]
    (keep-indexed
     (fn [i word]
       (let [i1 (+ 2 (* step i))
             i2 (+ i1 2)
             j1 (-> (- n 3 (count word)) rand-int (+ 2))]
         (when (< i2 m2)
           [i1 j1 word])))
     words)))

(defn- range2 [[i1 j1 word]]
  (for [i (range i1 (+ i1 2)) j (range j1 (+ j1 (count word)))]
    [i j]))

(defn- coords->regions [placements]
  (->> placements
       (mapcat
        #(let [r (range2 %)]
          (for [coord r] [coord r])))
       (into {})))

(def bit-tests
  (mapv
   (fn [i]
     (filter #(bit-test i %) (range 4)))
   (range 16)))
(defn- neighbor [[i j] direction]
  (case direction
    0 [(dec i) j]
    1 [i (inc j)]
    2 [(inc i) j]
    3 [i (dec j)]))
(defn- get-grid [v [i j]]
  (-> i (* n) (+ j) v))

(defn- search-candidates [grid this]
  (->> this
       (get-grid grid)
       bit-tests
       (map
        (fn [direction]
          {:this this
           :next (neighbor this direction)
           :direction direction
           :opposing-direction (case direction 0 2 1 3 2 0 3 1)}))))

(defn- bit-clear-v
  ([v [i j] k]
   (update v (+ (* i n) j) bit-clear k))
  ([v i j k]
   (update v (+ (* i n) j) bit-clear k)))
(def corner-clears
  [12 7 3 6])
(defn- corner-clear [v i j k]
  (update v (+ (* i n) j) bit-and (corner-clears k)))

(defn- clear-h [v i edge]
  (reduce #(bit-clear-v %1 i %2 edge) v (range n)))
(defn- clear-v [v j edge]
  (reduce #(bit-clear-v %1 %2 j edge) v (range m)))

(defn- clear-square [v [i1 j1 word]]
  (reduce
   (fn [v j]
     (-> v
         (corner-clear i1 j 1)
         (corner-clear i1 (inc j) 2)
         (corner-clear (inc i1) (inc j) 3)
         (corner-clear (inc i1) j 0)))
   v
   (range j1 (+ j1 (count word) -1))))

(def virgin
  (-> (* m n)
      (repeat 15)
      vec
      (clear-h 0 0)
      (clear-h (dec m) 2)
      (clear-v 0 3)
      (clear-v (dec n) 1)))

(defn- clear-squares [placements]
  (reduce clear-square virgin placements))

(defn- maze* [grid coord visited coords->regions stack]
  (let [to-mark (coords->regions coord [coord])
        visited (apply conj visited to-mark)
        candidates (->> to-mark
                        (mapcat #(search-candidates grid %))
                        (remove #(-> % :next visited)))]
    (if (empty? candidates)
      (if (empty? stack)
        grid
        (recur grid (peek stack) visited coords->regions (pop stack)))
      (let [{:keys [this next direction opposing-direction]} (rand-nth candidates)]
        (-> grid
            (bit-clear-v this direction)
            (bit-clear-v next opposing-direction)
            (recur next visited coords->regions (conj stack this)))))))

(defn maze [words]
  (let [placements (place-words words)]
    (maze* (clear-squares placements) [0 0] #{} (coords->regions placements) [])))
