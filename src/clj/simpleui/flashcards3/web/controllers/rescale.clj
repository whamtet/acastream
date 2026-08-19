(ns simpleui.flashcards3.web.controllers.rescale
  (:require
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow])
  (:import
    [java.awt.image BufferedImage RescaleOp]
    javax.imageio.ImageIO))

(defn- percentile
  [histogram total p]
  (let [target (long (Math/ceil (* total p)))]
    (loop [i 0
           count 0]
      (let [count (+ count (aget histogram i))]
        (if (>= count target)
          i
          (recur (inc i) count))))))

(defn- luminance-percentiles
  [^BufferedImage image]
  (let [histogram (int-array 256)
        width (.getWidth image)
        height (.getHeight image)
        total (* width height)]

    (dotimes [y height]
      (dotimes [x width]
        (let [rgb (.getRGB image x y)
              r (bit-and (bit-shift-right rgb 16) 0xff)
              g (bit-and (bit-shift-right rgb 8) 0xff)
              b (bit-and rgb 0xff)
              luminance (int (+ (* 0.299 r)
                                (* 0.587 g)
                                (* 0.114 b)))]
          (aset-int histogram luminance
                    (inc (aget histogram luminance))))))

    [(percentile histogram total 0.01)
     (percentile histogram total 0.99)]))

(defn- rescale-buffer [image]
  (let [[p01 p99] (luminance-percentiles image)
        contrast (/ 255.0 (- p99 p01))
        offset (- (* p01 contrast))
        op (RescaleOp. (float contrast)
                       (float offset)
                       nil)]
    (.filter op image nil)))

(defn rescale-all [query-fn slideshow_id]
  (doseq [f (slideshow/local-files query-fn slideshow_id)]
    (-> f
        ImageIO/read
        rescale-buffer
        (ImageIO/write "jpg" f))))
