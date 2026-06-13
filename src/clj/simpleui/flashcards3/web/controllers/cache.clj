(ns simpleui.flashcards3.web.controllers.cache
  (:require
    [simpleui.flashcards3.env :refer [dev?]]
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clj-http.lite.client :as client]
    [simpleui.response :as response])
  (:import
    java.io.File
    java.net.URL))

(def cache-dir (File. "cache"))
(.mkdir cache-dir)

(defn- split-suffix [path]
  (let [split (.split path "\\.")]
    [(string/join "." (butlast split))
     (last split)]))

(defn cache-file [src]
  (let [u (URL. src)
        [path suffix] (-> u .getPath split-suffix)]
    (-> (str path (.getQuery u))
        (.replace "/" "")
        (.replace "?" "")
        (.replace "&" "")
        (.replace "=" "")
        (str "." suffix)
        (->> (File. cache-dir)))))

(defn- web-stream [url]
  (:body
    (client/get url {:headers {"User-Agent" "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"}
                     :as :stream})))

(def cache-url (if dev? "/ncache2/" "/ncache/"))

(defn cache [src]
  (let [f (cache-file src)]
    (if (.exists f)
      (->> f .getName (str cache-url) response/redirect)
      (do
        (with-open [in (web-stream src)]
          (io/copy in f))
        (->> f .getName (str cache-url) response/redirect)))))
