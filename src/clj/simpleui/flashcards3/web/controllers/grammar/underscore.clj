(ns simpleui.flashcards3.web.controllers.grammar.underscore
  (:require
    [clojure.string :as string]))

(defn underscore-gen [lines]
  (let [sectionss (map #(-> % (str " ") (.split "_")) lines)]
    (assert (every? #(-> % count odd?) sectionss))
    (fn [i]
      (let [sections (rand-nth sectionss)
            n (count sections)
            j (mod (- n i i 2) n)]
        (->> sections
             (map-indexed
              (fn [jd s]
                (if (= j jd)
                  (-> s count (+ 2) (repeat \_) string/join)
                  s)))
             string/join
             .trim)))))
