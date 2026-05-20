(ns simpleui.flashcards3.web.controllers.local
  (:require
    [clojure.java.io :as io])
  (:import
    [java.io File]
    [javax.imageio ImageIO]
    [java.awt Color Graphics2D]
    [java.awt.image BufferedImage]))

(def local-dir (File. "local"))
(.mkdir local-dir)

(defn- format-img [x]
  (if (< x 1000)
    (format "%03d.jpg" x)
    (str x ".jpg")))

(defn- new-integer []
  (let [x (rand-int Integer/MAX_VALUE)]
    (if (->> x format-img (File. local-dir) .exists)
      (recur)
      x)))

(defn- png->jpg
  "Convert a PNG file to JPG.
   Transparent areas are filled with white."
  [file]
  (let [local-count (new-integer)
        jpg-file (->> local-count (format "%03d.jpg") (File. local-dir))
        png-img  (-> file :tempfile ImageIO/read)
        jpg-img  (BufferedImage.
                   (.getWidth png-img)
                   (.getHeight png-img)
                   BufferedImage/TYPE_INT_RGB)
        g        (.createGraphics jpg-img)]
    ;; Fill background (JPG has no alpha)
    (.setColor g Color/WHITE)
    (.fillRect g 0 0 (.getWidth jpg-img) (.getHeight jpg-img))
    ;; Draw PNG onto RGB image
    (.drawImage g png-img 0 0 nil)
    (.dispose g)
    (ImageIO/write jpg-img "jpg" jpg-file)
    [local-count local-count]))

(defn delete [index]
  (->> index
       format-img
       (File. local-dir)
       .delete))

(defn convert [files]
  (map png->jpg files))

(defn input-file [local_id]
  (->> local_id format-img (str "local/") File.))
(defn input-stream [local_id]
  (io/input-stream (input-file local_id)))

(def supported-types (ImageIO/getReaderMIMETypes))

(defn all-locals []
  (->> local-dir
       .listFiles
       (keep (fn [f]
               (when-let [n (->> f .getName (re-find #"\d+"))]
                 (Long/parseLong n))))
       set))
