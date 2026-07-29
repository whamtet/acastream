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
    :open? false}))

(defn- copy-file [{:keys [tempfile]}]
  (io/copy tempfile (File. share (.getName tempfile)))
  tempfile)

(defn- add-entry* [status name files]
  (-> status
      (update :files conj files)
      (update :names conj name)))
(defn add-entry [name files]
  (let [files (doall (map copy-file files))]
    (swap! share-status add-entry* name files)))

(defn open? []
  (:open? @share-status))

(defn toggle-open [open?]
  (swap! share-status assoc :open? open?)
  nil)

(defn delete-files []
  (doseq [file (:files @share-status)]
    (.delete file))
  (swap! share-status assoc :files [] :names [])
  nil)
