(ns simpleui.flashcards3.web.controllers.slideshow.mobile
  (:require
    [clojure.string :as string]))

(def ks (atom {}))

(defn- rand-str []
  (->> #(rand-nth "abcdefghijklmnopqrstuvwxyz")
       (repeatedly 10)
       string/join))

(defn get-key [slideshow_id]
  (let [k (rand-str)]
    (swap! ks assoc k slideshow_id)
    k))

(defn key->slideshow_id [key]
  (@ks key))
