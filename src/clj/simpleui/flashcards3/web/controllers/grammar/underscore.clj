(ns simpleui.flashcards3.web.controllers.grammar.underscore
  (:require
    [clojure.string :as string]))

(defn underscore-gen [lines]
  (let [sectionss (map #(-> % (str " ") (.split "_")) lines)]
    (assert (every? #(-> % count odd?) sectionss))
    (fn [i]
      (let [sections (rand-nth sectionss)
            n (bit-shift-right (count sections) 1)
            j (mod (- n i 1) n)
            k (inc (* 2 j))]
        (->> sections
             (map-indexed
              (fn [kd s]
                (if (= k kd)
                  (-> s count (+ 2) (repeat \_) string/join)
                  s)))
             (string/join " ")
             .trim)))))
