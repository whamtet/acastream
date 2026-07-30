(ns simpleui.flashcards3.web.controllers.share
  (:require
    [clojure.java.io :as io])
  (:import
    [java.io File]))

(def share (File. "share"))
(.mkdir share)

(def share-status
  (atom
   {:files []
    :names []
    :open? false
    :force-images? false}))

(defn- copy-file [{:keys [tempfile filename content-type]}]
  (let [f2 (File. share filename)]
    (io/copy tempfile f2)
    {:f f2 :content-type content-type}))

(defn- add-entry* [status name files]
  (-> status
      (update :files conj files)
      (update :names conj name)))
(defn add-entry [name files]
  (let [files (mapv copy-file files)]
    (swap! share-status add-entry* name files)))

(defn open? []
  (:open? @share-status))
(defn force-images? []
  (:force-images? @share-status))

(defn toggle-open [open?]
  (swap! share-status assoc :open? open?)
  nil)
(defn force-images [force?]
  (swap! share-status assoc :force-images? force?)
  nil)

(defn delete-files []
  (doseq [file (:files @share-status)]
    (.delete file))
  (swap! share-status assoc :files [] :names [])
  nil)

(defn get-submissions []
  (let [{:keys [names files]} @share-status]
    (map list names files)))

(defn get-submission [i j]
  (let [{:keys [f content-type]} (get-in @share-status [:files i j])]
    [content-type (io/input-stream f)]))
