(ns simpleui.flashcards3.web.controllers.pdf-maze
  (:require
    [clj-pdf.core :as pdf]
    [simpleui.flashcards3.web.controllers.maze :as maze]
    [simpleui.flashcards3.web.controllers.pdf-jtd :as pdf-jtd]
    [simpleui.flashcards3.util :as util])
  (:import [java.io File
            ByteArrayOutputStream ByteArrayInputStream]))

(def mi (/ 1 maze/m))
(def ni (/ 1 maze/n))
(def buff 2)
(def tot (- 100 buff buff))
(defn- pos [a b]
  (+ buff (* tot a b)))
(defn- diff [b]
  (* tot b))

(defn- h-line [i j]
  [:line {:x1 (pos j ni) :x2 (pos (inc j) ni) :y1 (pos i mi) :y2 (pos i mi)
          :stroke "black" :stroke-width 0.3}])
(defn- v-line [i j]
  [:line {:x1 (pos j ni) :x2 (pos j ni) :y1 (pos i mi) :y2 (pos (inc i) mi)
          :stroke "black" :stroke-width 0.3}])
(defn- square [i j]
  [:rect {:x (pos j ni) :y (pos i mi) :width (diff ni) :height (diff mi)
          :fill "yellow"}])
(defn- m [x y font-size]
  {:x x :y y :font-size font-size :font-family :courier :fill "black"})
(defn- text [i j c]
  [:text (m (+ (pos j ni) 1) (+ (pos i mi) 1.6) 1.8) (str c)])

(defn- cell [path?]
  (fn [h c x]
    (let [i (long (/ h maze/n))
          j (mod h maze/n)]
      (list
       ;; (when (path? [i j]) (square i j)) ;; for debugging
       (when (and (zero? i) (bit-test x 0)) (h-line i j))
       (when (bit-test x 1) (v-line i (inc j)))
       (when (bit-test x 2) (h-line (inc i) j))
       (when (and (zero? j) (bit-test x 3)) (v-line i j))
       (case c
         :start [:text (m (+ (pos j ni) 0.4) (+ (pos i mi) 0.4) 4) "↓"]
         :end [:text (m (+ (pos j ni) 0.4) (+ (pos i mi) 3.5) 4) "↓"]
         (text i j c))))))

(defn pdf [query-fn slideshow_id]
  (let [out (ByteArrayOutputStream.)
        [placements maze path] (maze/maze query-fn slideshow_id)]
    ;; produce PDF in another thread
    (pdf/pdf
     [{:size :a4
       :footer false}
      [:svg {:translate [25 30] :scale [5.3 7.4]}
       (pdf-jtd/svg-s
        (-> path set cell (mapcat (range) placements maze)))]]
     out)
    (ByteArrayInputStream. (.toByteArray out))))
