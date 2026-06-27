(ns simpleui.flashcards3.web.controllers.reading
  (:require
    [simpleui.flashcards3.web.controllers.util :as util]))

(defn add-reading [query-fn reading_name]
  (when-not (query-fn :get-reading-name {:reading_name reading_name})
    (query-fn :insert-reading {:reading_name reading_name})))

(defn get-readings [query-fn]
  (->> (query-fn :get-readings {})
       (sort-by :reading_name util/compare-names)))

(defn empty-s? [^String s]
  (or (not s)
      (-> s .trim empty?)))

(defn get-reading [query-fn reading_id]
  (query-fn :get-reading {:reading_id reading_id}))
(defn get-details [query-fn reading_id]
  (:details
    (get-reading query-fn reading_id)))
(defn get-paragraphs [query-fn reading_id]
  (-> (get-details query-fn reading_id)
      .trim
      (.split "\n\n")
      seq))

(defn reading-details [query-fn reading_id details]
  (query-fn :reading-details {:reading_id reading_id :details details}))

(defn reading-name [query-fn reading_id reading_name]
  (if (empty-s? reading_name)
    (do
      (query-fn :reading-delete {:reading_id reading_id})
      false)
    (query-fn :reading-name {:reading_id reading_id :reading_name reading_name})))
