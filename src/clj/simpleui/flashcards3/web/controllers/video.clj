(ns simpleui.flashcards3.web.controllers.video
  (:require
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow])
  (:import
    java.io.File))

;; https://flashcards.simpleui.io/ncache/video/The_Floor_Is_Lava.3.webm

(def videos (File. "cache/video"))

(defn- video-fragments [prefix]
  (->> videos
       .listFiles
       (map #(.getName %))
       (filter #(.startsWith % prefix))
       sort))

(defn- partition-all-big [n s]
  (if (< (count s) (* 2 n))
    (list s)
    (cons (take n s) (partition-all-big n (drop n s)))))

(defn- video-info* [slides prefix]
  (let [fragments (video-fragments prefix)
        num-prefragments (dec (count fragments))
        n (-> (/ (count slides) num-prefragments)
              Math/floor
              (max 1))]
    (conj
     (mapv conj (partition-all-big n slides) fragments)
     (drop num-prefragments fragments))))

(defn video-info [query-fn slideshow_id prefix]
  (video-info*
   (slideshow/get-slideshow-slides-large query-fn slideshow_id)
   prefix))
