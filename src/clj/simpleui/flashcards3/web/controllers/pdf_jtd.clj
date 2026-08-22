(ns simpleui.flashcards3.web.controllers.pdf-jtd
  (:require
    [clojure.string :as string]
    [hiccup.core :as h]
    [clj-pdf.core :as pdf]
    [simpleui.flashcards3.util :as util])
  (:import [java.io File
            ByteArrayOutputStream ByteArrayInputStream]))

(defn svg-s [& body]
  (str
   "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
           <!DOCTYPE svg>"
   (h/html
    [:svg {:xmlns "http://www.w3.org/2000/svg"
           :width "100"
           :height "100"
           :viewBox "0 0 100 100"} body])))

(defn- point [i x y]
  [:g
   [:circle {:cx (str (double x) "%")
             :cy (str (double y) "%")
             :r 0.2
             :fill "red"}]
   [:text {:x (str (double x) "%")
           :y (str (double y) "%")
           :dy "-0.5em"          ;; lift text slightly above the point
           :text-anchor "middle" ;; center horizontally
           :font-size 1.5
           :fill "black"}
    (str (inc i))]])

(defn- picture-label [i x y]
  [:text {:x (str (double x) "%")
          :y (str (double y) "%")
          :font-size 5
          :fill "black"}
   (str (inc i))])

(def margin 2)
(def x-inc [margin (+ 50 margin) margin (+ 50 margin) margin (+ 50 margin)])
(def x-scale (* (- 50 (* 2 margin)) 0.01))
(def y-inc [(* margin 2/3) (* margin 2/3)
            (+ 100/3 (* margin 2/3)) (+ 100/3 (* margin 2/3))
            (+ 200/3 (* margin 2/3)) (+ 200/3 (* margin 2/3))])
(def y-scale (* (- 100/3 (* 2 margin)) 0.01))

(defn- points [[i points]]
  (list
   (picture-label i (+ (x-inc i) 1) (+ 4.5 (y-inc i)))
   (map-indexed
    (fn [j [_ x y]]
      (point j (+ (* x-scale x) (x-inc i)) (+ (* y-scale y) (y-inc i))))
    points)))

(def margin 20)
(def cell-height 240)

(defn- parse-longs [s]
  (if (string? s)
    [(Long/parseLong s)]
    (map #(Long/parseLong %) s)))
(defn- parse-doubles [s]
  (if (string? s)
    [(Double/parseDouble s)]
    (map #(Double/parseDouble %) s)))

(defn pdf [{:keys [is xs ys words]}]
  (let [out (ByteArrayOutputStream.)
        is (parse-longs is)
        xs (parse-doubles xs)
        ys (parse-doubles ys)
        words (->> is (filter #(< % 6)) distinct (map (read-string words)) shuffle (string/join ", "))]
    ;; produce PDF in another thread
    (pdf/pdf
     [{:size :a4
       :left-margin   margin
       :right-margin  margin
       :top-margin    margin
       :bottom-margin margin
       :footer {:text words :page-numbers false}}

      [:pdf-table {:width-percent 100}
       [50 50]
       [[:pdf-cell {:height cell-height}] [:pdf-cell {:height cell-height}]]
       [[:pdf-cell {:height cell-height}] [:pdf-cell {:height cell-height}]]
       [[:pdf-cell {:height cell-height}] [:pdf-cell {:height cell-height}]]]

      [:svg {:translate [10 46] :scale [5.75 7.2]}
       (->> (map list is xs ys)
            (filter (fn [[i]] (< i 6)))
            (group-by first)
            (map points)
            svg-s)]

      ]
     out)
    (ByteArrayInputStream. (.toByteArray out))))
