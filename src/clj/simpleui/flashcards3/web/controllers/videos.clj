(ns simpleui.flashcards3.web.controllers.videos
  (:import
    java.io.File))

(def videos-dir (File. "cache/video"))

(defn videos []
  (->> videos-dir
       .listFiles
       (filter #(let [n (.getName %)]
                 (.endsWith n ".mp4")))))
