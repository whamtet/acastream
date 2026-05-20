(ns simpleui.flashcards3.web.controllers.pdf-bingo
  (:require
    [clojure.java.io :as io]
    [clj-pdf.core :as pdf]
    [simpleui.flashcards3.web.controllers.cache :as cache]
    [simpleui.flashcards3.web.controllers.local :as local]
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow])
  (:import [java.io File
            ByteArrayOutputStream ByteArrayInputStream]))

(defn- slurp-icon [s]
  (->> s (format "icons/%s.svg") io/resource slurp))

(def svgs
  (map slurp-icon ["circle" "star" "triangle"]))

;; assume all images are either cached or local
(defn img-url [src]
  (let [f (if (string? src) (cache/cache-file src) (local/input-file src))]
    (when (.exists f)
      (str f))))

(defn reference-map [srcs]
  (->> srcs distinct (map img-url) prn))

(def margin 18)
(def scale 0.16)

(defn- img [src]
  [:image {:xscale scale :yscale scale} src])

(defn- row [keys]
  (vec
   (for [_ (range 5)]
     [:pdf-cell
      (img (rand-nth keys))])))

(defn- svg [h]
  (let [i (long (/ h 5))
        j (mod h 5)
        x (+ 122 (* j 163))
        y (+ 84 (* i 85))]
    [:svg {:translate [x y]} (rand-nth svgs)]))

(defn- page [keys]
  #(list
    [:pdf-table
     {:width-percent 100 :border-width 2}
     [20 20 20 20 20]
     (row keys)
     (row keys)
     (row keys)
     (row keys)
     (row keys)]
    (map svg (range 25))
    [:pagebreak]))

(defn pdf [query-fn pages slideshow_id]
  (let [srcs (->> (slideshow/get-slideshow-slides query-fn slideshow_id)
                  (map second)
                  distinct
                  (keep img-url))
        out (ByteArrayOutputStream.)]
    ;; produce PDF in another thread
    (pdf/pdf
         [{:size :a4
           :orientation :landscape
           :left-margin   margin
           :right-margin  margin
           :top-margin    margin
           :bottom-margin margin}
          (if (empty? srcs)
            "No src"
            (->> srcs page (repeatedly pages)))
          ]
         out)
    (ByteArrayInputStream. (.toByteArray out))))
