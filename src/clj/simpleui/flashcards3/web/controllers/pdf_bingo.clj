(ns simpleui.flashcards3.web.controllers.pdf-bingo
  (:require
    [clojure.java.io :as io]
    [clj-pdf.core :as pdf]
    [simpleui.flashcards3.web.controllers.pdf-bingo.img :as img]
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow])
  (:import [java.io File
            ByteArrayOutputStream ByteArrayInputStream]))

(defn- slurp-icon [s]
  (->> s (format "icons/%s.svg") io/resource slurp))

(def svgs
  (map slurp-icon ["circle" "star" "triangle"]))

(def margin 18)

(defn- img [{:keys [scale src]}]
  [:image {:xscale scale :yscale scale} src])

(defn- row [keys]
  (vec
   (for [key keys]
     [:pdf-cell (img key)])))

(defn- svg [h img]
  (let [i (long (/ h 5))
        j (mod h 5)
        x (+ 132 (* j 163))
        y (+ 88 (* i 88))]
    [:svg {:translate [x y]} img]))

(defn shuffle-keys [keys]
  (->> keys
    shuffle
    cycle
    (take 25)
    shuffle
    (partition 5)))

(defn- shuffle-svgs []
  (->> svgs
       shuffle
       cycle
       (take 25)
       shuffle))

(defn- page [keys]
  #(list
    `[:pdf-table
      {:width-percent 100 :border-width 2}
      [20 20 20 20 20]
      ~@(map row (shuffle-keys keys))]
    (map-indexed svg (shuffle-svgs))
    [:pagebreak]))

(defn pdf [query-fn pages scale slideshow_id]
  (let [srcs (->> (slideshow/get-slideshow-slides query-fn slideshow_id)
                  (map second)
                  distinct
                  (img/make-scale scale)) 
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
