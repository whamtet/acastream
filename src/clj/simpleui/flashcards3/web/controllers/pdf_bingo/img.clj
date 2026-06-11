(ns simpleui.flashcards3.web.controllers.pdf-bingo.img
  (:require
    [simpleui.flashcards3.web.controllers.cache :as cache]
    [simpleui.flashcards3.web.controllers.local :as local]))

;; assume all images are either cached or local
(defn- img-url [src]
  (let [f (if (string? src) (cache/cache-file src) (local/input-file src))]
    (when (.exists f) f)))


(defn make-scale [scale srcs]
  (for [src srcs
        :let [src (img-url src)]
        :when src]
    {:scale scale :src (str src)}))
