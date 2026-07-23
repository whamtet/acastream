(ns simpleui.flashcards3.web.controllers.pdf
  (:require
    [clj-pdf.core :as pdf]
    [simpleui.flashcards3.web.controllers.local :as local]
    [clojure.string :as string]
    [clj-http.lite.client :as client]
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow])
  (:import
    javax.imageio.ImageIO
    java.awt.geom.AffineTransform
    java.awt.image.AffineTransformOp
    java.awt.image.BufferedImage
    java.io.ByteArrayOutputStream
    java.io.ByteArrayInputStream))

(defn- slurp-img* [url]
  (ImageIO/read
   (if (string? url)
     (:body
       (client/get url {:headers {"User-Agent" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"}
                        :as :stream}))
     (local/input-stream url))))

(defn- slurp-img [[url2 url1]]
  (try
    (slurp-img* url1)
    (catch clojure.lang.ExceptionInfo e
      (try
        (slurp-img* url2)
        (catch clojure.lang.ExceptionInfo e
          (-> e ex-data :status))))))

(defn- rotate-scale [scale translate]
  (doto (AffineTransform.)
        (.translate translate 0.)
        (.scale scale scale)
        (.rotate (* 0.5 Math/PI))))

(def max-width 500)
(def max-height 842)
(defn- rot-scale [img]
  (let [rotate? (> (.getWidth img) (.getHeight img))
        short-dim (min (.getWidth img) (.getHeight img))
        long-dim (max (.getWidth img) (.getHeight img))
        x-scale (/ max-width short-dim)
        y-scale (/ max-height long-dim)
        scale (min x-scale y-scale)]
    (if (and (= 1 scale) (not rotate?))
      img
      (let [scale (double scale)
            out (BufferedImage.
                  (-> short-dim (* scale) long)
                  (-> long-dim (* scale) long)
                  BufferedImage/TYPE_INT_ARGB)
            transform (if rotate?
                        (rotate-scale scale (* short-dim scale))
                        (AffineTransform. scale 0. 0. scale 0. 0.))]
        (-> (AffineTransformOp. transform AffineTransformOp/TYPE_BICUBIC)
            (.filter img out))
        out))))

(defn- img-el [x]
  (let [img (slurp-img x)]
    (if (or (number? img) (nil? img))
      [:paragraph (format "Error: %s for %s" img x)]
      [:image (rot-scale img)])))

(defn- trim-lines [s]
  (->> (.split s "\n")
       (remove #(-> % .trim empty?))
       (string/join "\n")))

(defn- pdf [{{:keys [slides]} :details :keys [slideshow_name]}]
  (let [out (ByteArrayOutputStream.)]
    (pdf/pdf
     [{:footer {:text (str slideshow_name " Page")}}
      (pmap img-el slides)]
     out)
    (-> out .toByteArray ByteArrayInputStream.)))

(defn get-pdf [query-fn slideshow_id]
  (->> slideshow_id
       (slideshow/get-slideshow-details2 query-fn)
       pdf))
