(ns simpleui.flashcards3.web.controllers.maze
  (:require
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]))

(def m 5)
(def n 4)
(def m2 (- m 4))
(def n2 (- n 4))

(defn- place-words [words]
  (let [words (->> words (map count) (filter #(< 1 % n2)))
        step (->> words count (/ m2) long (max 3))]
    (->>
     words
     (map-indexed
      (fn [i len]
        (let [i1 (+ 2 (* step i))
              j1 (-> (- n 3 len) rand-int (+ 2))
              i2 (+ i1 2)
              j2 (+ j1 len)]
          (when (< i2 m2)
            (for [i (range i1 i2) j (range j1 j2)]
              [[i j] [i1 j1 i2 j2]])))))
     (apply concat)
     (into {}))))

(def bit-tests
  (mapv
   (fn [i]
     (filter #(bit-test i %) (range 4)))
   (range 16)))

(defn- bit-clear-v [v i j k]
  (update v (+ (* i n) j) bit-clear k))
(def corner-clears
  [12 7 3 6])
(defn- corner-clear [v i j k]
  (update v (+ (* i n) j) bit-and (corner-clears k)))


(defn- get-v [v i j]
  (-> i (* n) (+ j) v))

(defn- clear-h [v i edge]
  (reduce #(bit-clear-v %1 i %2 edge) v (range n)))
(defn- clear-v [v j edge]
  (reduce #(bit-clear-v %1 %2 j edge) v (range m)))

(defn- clear-square [v [i1 j1 _ j2]]
  (reduce
   (fn [v j]
     (-> v
         (corner-clear i1 j 1)
         (corner-clear i1 (inc j) 2)
         (corner-clear (inc i1) (inc j) 3)
         (corner-clear (inc i1) j 0)))
   v
   (range j1 (dec j2))))

(def virgin
  (-> (* m n)
      (repeat 15)
      vec
      (clear-h 0 0)
      (clear-h (dec m) 2)
      (clear-v 0 3)
      (clear-v (dec n) 1)))

