(ns simpleui.flashcards3.web.controllers.memory
  (:require
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]))

(defn- get-width [i double-size]
  (if (>= (* (dec i) i) double-size)
    i
    (recur (inc i) double-size)))

(def LIMIT 15)
(defn permutation [query-fn slideshow_id]
  (let [n (count (slideshow/get-slideshow-slides query-fn slideshow_id))
        width (->> n (min LIMIT) (* 2) (get-width 2))
        indices (->> n range shuffle (take LIMIT))]
    [width
     (->> (concat indices indices (repeat nil))
          (take (* (dec width) width))
          shuffle
          vec)]))

(defn images [query-fn slideshow_id permutation]
  (let [v (slideshow/get-slideshow-slides query-fn slideshow_id)]
    (map #(get v %) permutation)))
