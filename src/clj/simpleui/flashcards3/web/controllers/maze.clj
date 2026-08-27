(ns simpleui.flashcards3.web.controllers.maze
  (:require
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]))

(def m 40)
(def n 30)
(def m2 (- m 4))
(def n2 (- n 4))

(defn- place-words [words]
  (when-let [words (->> words (filter #(< 1 (count %) n2)) not-empty)]
    (let [step (->> words count (/ m2) long (max 3))]
      (keep-indexed
       (fn [i word]
         (let [i1 (+ 2 (* step i))
               i2 (+ i1 2)
               j1 (-> (- n 3 (count word)) rand-int (+ 2))]
           (when (< i2 (dec m))
             [i1 j1 word])))
       words))))

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

(defn- bit-clear-v [v [i j] k]
  (update v (+ (* i n) j) bit-clear k))
(defn- assoc-v [v i j x]
  (assoc v (+ (* i n) j) x))

(def corner-clears
  [12 9 3 6])
(defn- corner-clear [v i j k]
  (update v (+ (* i n) j) bit-and (corner-clears k)))

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
      (update 0 bit-clear 0)
      (update (dec (* m n)) bit-clear 2)))
(def virgin-words
  (-> (* m n)
      (repeat nil)
      vec
      (assoc 0 :start)
      (assoc (dec (* m n)) :end)))
(def borders
  (set
   (concat
    (mapcat
     (fn [i]
       [[i -1] [i n]])
     (range m))
    (mapcat
     (fn [j]
       [[-1 j] [m j]])
     (range -1 (inc n))))))

(defn- clear-squares [placements]
  (reduce clear-square virgin placements))

(defn- maze*** [grid peak-stack coord visited coords->regions stack]
  (let [to-mark (coords->regions coord [coord])
        visited (apply conj visited to-mark)
        candidates (->> to-mark
                        (mapcat #(search-candidates grid %))
                        (remove #(-> % :next visited)))
        peak-stack (if (= [(dec m) (dec n)] coord)
                     stack
                     peak-stack)]
    (if (empty? candidates)
      (if (empty? stack)
        (list grid peak-stack)
        (recur grid peak-stack (peek stack) visited coords->regions (pop stack)))
      (let [{:keys [this next direction opposing-direction]} (rand-nth candidates)]
        (-> grid
            (bit-clear-v this direction)
            (bit-clear-v next opposing-direction)
            (recur peak-stack next visited coords->regions (conj stack this)))))))

(defn- region-count [[_ peak-stack] coords->regions]
  (->> peak-stack (map coords->regions) distinct count))

(defn- maze** [grid coords->regions]
  (->> #(maze*** grid nil [0 0] borders coords->regions [])
       (repeatedly 10)
       (apply max-key #(region-count % coords->regions))))

(defn- insert-word [v [i j word]]
  (->> word
       seq
       shuffle
       (map-indexed list)
       (reduce
        (fn [v [j2 c]]
          (-> v (assoc-v i (+ j j2) c) (assoc-v (inc i) (+ j j2) \_)))
        v)))

(defn- maze* [words]
  (let [placements (place-words words)]
    (conj
     (maze** (clear-squares placements) (coords->regions placements))
     (reduce insert-word virgin-words placements))))

(defn maze [query-fn slideshow_id]
  (maze* (slideshow/get-slideshow-notes query-fn slideshow_id)))
