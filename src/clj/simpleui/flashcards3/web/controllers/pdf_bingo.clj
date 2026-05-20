(ns simpleui.flashcards3.web.controllers.pdf-bingo
  (:require
    [clj-pdf.core :as pdf]
    [simpleui.flashcards3.web.controllers.cache :as cache]
    [simpleui.flashcards3.web.controllers.local :as local]
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]
    [simpleui.flashcards3.util :as util])
  (:import [java.io File
            ByteArrayOutputStream ByteArrayInputStream]))

;; assume all images are either cached or local
(defn img-url [src]
  (if (string? src)
    (-> src cache/cache-file str)
    (local/input-file src)))

(defn reference-map [srcs]
  (->> srcs distinct (map img-url) prn))

(def margin 18)

(defn- img [src]
  [:image {:xscale 0.15 :yscale 0.15} src])

(defn- row [keys]
  (vec
   (for [_ (range 5)]
     [:pdf-cell
      (img (rand-nth keys))])))

(defn- page [keys]
  #(list
    [:pdf-table
     {:border false}
     [20 20 20 20 20]
     (row keys)
     (row keys)
     (row keys)
     (row keys)
     (row keys)]
    [:pagebreak]))

(defn pdf [query-fn pages slideshow_id]
  (let [srcs (->> (slideshow/get-slideshow-slides query-fn slideshow_id)
                  (map second)
                  distinct
                  (map img-url))
        out (ByteArrayOutputStream.)]
    ;; produce PDF in another thread
    (pdf/pdf
         [{:size :a4
           :left-margin   margin
           :right-margin  margin
           :top-margin    margin
           :bottom-margin margin}
          (->> srcs page (repeatedly pages))
          ]
         out)
    (ByteArrayInputStream. (.toByteArray out))))
