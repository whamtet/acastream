(ns simpleui.flashcards3.web.controllers.img-search
  (:require
    [clj-http.lite.client :as client]
    [simpleui.flashcards3.util :as util :refer [defm-dev]]))

(def pexels-api-key (System/getenv "PEXELS_API_KEY"))
(def pixabay-api-key (System/getenv "PIXABAY_API_KEY"))

(defn- get-src-pexels [{{:keys [medium small]} :src :as x}]
  [small medium])
(defn- get-src-pixabay [{:keys [previewURL webformatURL]}]
  [previewURL webformatURL])

(defm-dev get-pics [q]
  (->>
   (client/get "https://api.pexels.com/v1/search"
               {:query-params {:query q}
                :headers {"Authorization" pexels-api-key}})
   :body
    util/read-str
   :photos
    (map get-src-pexels)))

(defm-dev get-icons [q]
  (->>
   (client/get "https://pixabay.com/api/"
               {:query-params {:key pixabay-api-key
                               :q q
                               :image_type "illustration"}})
   :body
    util/read-str
   :hits
    (map get-src-pixabay)))

(defn get-langeek [q]
  (->
   "https://api.langeek.co/v1/cs/en/vi/word/?term=%s&filter=,inCategory,withExamples"
   (format q)
   (client/get {})
   :body
   util/read-str
   first
   :translation
   :wordPhoto
   :photo))
