(ns simpleui.flashcards3.web.controllers.memory
  (:require
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]))

(defn- get-width [i double-size]
  (if (>= (* (dec i) i) double-size)
    i
    (recur (inc i) double-size)))

(def LIMIT 15)
(defn permutation [query-fn slideshow_id]
  (let [n (min (count (slideshow/get-slideshow-slides query-fn slideshow_id)) LIMIT)
        width (->> n (* 2) (get-width 2))]
    [width
     (->> (concat (range n) (range n) (repeat nil))
          (take (* (dec width) width))
          shuffle
          vec)]))

(defn images [query-fn slideshow_id permutation]
  (let [v (->> (slideshow/get-slideshow-slides query-fn slideshow_id)
               shuffle
               (take LIMIT)
               vec)]
    (map #(get v %) permutation)))
