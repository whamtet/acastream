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

(defn- h-line [i j]
  [:line {:x1 (* 100 j ni) :x2 (* 100 (inc j) ni) :y1 (* 100 i mi) :y2 (* 100 i mi)
          :stroke "black" :stroke-width 0.3}])
(defn- v-line [i j]
  [:line {:x1 (* 100 j ni) :x2 (* 100 j ni) :y1 (* 100 i mi) :y2 (* 100 (inc i) mi)
          :stroke "black" :stroke-width 0.3}])
(defn- text [i j c]
  [:text {:x (+ (* 100 j ni) 1) :y (+ (* 100 i mi) 1.55) :font-size 1.8 :fill "black"} (str c)])
;; "M4.5 10.5 12 3m0 0 7.5 7.5M12 3v18"
(defn- icon [i j icon]
  [:g {:transform (str "translate(" (+ (* 100 j ni)) ", " (+ (* 100 i mi)) ")")}
   [:g {:transform (str "scale(1 1)")}
    [:path {#_#_#_#_:stroke-linecap "round" :stroke-linejoin "round" :d icon}]]])

(defn- cell [h c x]
  (let [i (long (/ h maze/n))
        j (mod h maze/n)]
    (list
     (when (and (zero? i) (bit-test x 0)) (h-line i j))
     (when (bit-test x 1) (v-line i (inc j)))
     (when (bit-test x 2) (h-line (inc i) j))
     (when (and (zero? j) (bit-test x 3)) (v-line i j))
     (case c
       :start (prn (icon i j "M19.5 13.5 12 21m0 0-7.5-7.5M12 21V3"))
       :end (prn (icon i j "M19.5 13.5 12 21m0 0-7.5-7.5M12 21V3"))
       (text i j c))
     )))

(defn pdf [query-fn slideshow_id]
  (let [out (ByteArrayOutputStream.)
        [placements maze] (maze/maze query-fn slideshow_id)]
    ;; produce PDF in another thread
    (pdf/pdf
     [{:size :a4
       :footer false}
      [:svg {:translate [25 32] :scale [5.3 7.4]}
       (pdf-jtd/svg-s
        (mapcat cell (range) placements maze))]]
     out)
    (ByteArrayInputStream. (.toByteArray out))))
