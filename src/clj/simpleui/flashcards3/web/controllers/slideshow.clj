(ns simpleui.flashcards3.web.controllers.slideshow
  (:require
    [simpleui.flashcards3.qr :as qr]
    [simpleui.flashcards3.web.controllers.local :as local]
    [simpleui.flashcards3.web.controllers.slideshow.mobile :as mobile]
    [simpleui.flashcards3.web.controllers.slideshow.delete :as delete]
    [simpleui.flashcards3.web.controllers.util :as util])
  (:import
    java.util.Date))

(defn add-slideshow [query-fn slideshow_name]
  (when-not (query-fn :get-slideshow-name {:slideshow_name slideshow_name})
    (-> (query-fn :insert-slideshow {:slideshow_name slideshow_name})
        first
        :slideshow_id)))

(defn get-slideshows [query-fn tab]
  (let [grouped (util/group-by-other #(re-find #"\w+" (:slideshow_name %))
                                     (query-fn :get-slideshows {}))]
    [(->> tab
          grouped
          (sort-by :slideshow_name util/compare-names)
          (map #(update % :details delete/read-details)))
     (->> grouped keys sort)]))

(defn- recent? [x]
  (and x
       (<
        (.getTime (java.util.Date.))
        (+ (.getTime x)
           (* 1000 60 60 24 2)))))
(defn- compare-updated-name
  [{n1 :slideshow_name {u1 :updated} :details}
   {n2 :slideshow_name {u2 :updated} :details}]
  (cond
    ;; recent decending
    (and (recent? u1) (recent? u2)) (compare u2 u1)
    (recent? u1) -1
    (recent? u2) 1
    :else ;; name ascending
    (compare n1 n2)))

(defn get-slideshows-summary [query-fn]
  (->> (query-fn :get-slideshows {})
       (map #(update % :details delete/read-details))
       (sort-by identity compare-updated-name)))

(defn get-slideshow-details [query-fn slideshow_id]
  (-> (query-fn :get-slideshow {:slideshow_id slideshow_id})
      :details
      delete/read-details))
(defn get-slideshow-slides [query-fn slideshow_id]
  (:slides
    (get-slideshow-details query-fn slideshow_id)))
(defn get-slideshow-notes [query-fn slideshow_id]
  (:notes
    (get-slideshow-details query-fn slideshow_id)))
(defn get-slideshow-slides-edit [query-fn slideshow_id]
  (let [{:keys [slides notes]} (get-slideshow-details query-fn slideshow_id)]
    (map (fn [[medium] note] [medium note]) slides notes)))
(defn get-slideshow-slides-notes [query-fn slideshow_id]
  (let [{:keys [slides notes]} (get-slideshow-details query-fn slideshow_id)]
    (map list slides notes)))

(defn get-slideshow-name [query-fn slideshow_id]
  (:slideshow_name (query-fn :get-slideshow {:slideshow_id slideshow_id})))

(defn update-slideshow-name [query-fn slideshow_id slideshow_name]
  (query-fn :slideshow-name {:slideshow_id slideshow_id :slideshow_name slideshow_name}))

(defn- slideshow-details [query-fn slideshow_id details]
  (query-fn :slideshow-details {:slideshow_id slideshow_id :details (pr-str details)}))

(defn duplicate-slideshow [query-fn slideshow_id]
  (when-let [{:keys [slideshow_name details]} (query-fn :get-slideshow {:slideshow_id slideshow_id})]
    (let [[{:keys [slideshow_id]}] (query-fn :insert-slideshow {:slideshow_name (str slideshow_name " Copy")})]
      (query-fn :slideshow-details {:slideshow_id slideshow_id :details details})
      slideshow_id)))

(defn delete-slideshow [query-fn slideshow_id]
  (query-fn :slideshow-delete {:slideshow_id slideshow_id})
  (delete/clean-files query-fn))

(defn- update-slides [query-fn slideshow_id f & args]
  (as-> (get-slideshow-details query-fn slideshow_id) $
        (update $ :slides #(apply f % args))
        (update $ :notes #(apply f % args))
        (slideshow-details query-fn slideshow_id $)))

(defn conj-slideshow [query-fn slideshow_id x]
  (as-> (get-slideshow-details query-fn slideshow_id) $
        (update $ :slides conj x)
        (update $ :notes conj "")
        (slideshow-details query-fn slideshow_id $)))
(defn- conj-slideshow-note [query-fn slideshow_id slide note]
  (as-> (get-slideshow-details query-fn slideshow_id) $
        (update $ :slides conj slide)
        (update $ :notes conj note)
        (assoc $ :updated (Date.))
        (slideshow-details query-fn slideshow_id $)))

(defn concat-slideshow [query-fn slideshow_id images]
  (as-> (get-slideshow-details query-fn slideshow_id) $
        (update $ :slides into (local/convert images))
        (update $ :notes into (repeat (count images) ""))
        (slideshow-details query-fn slideshow_id $)))

(defn slideshow-note [query-fn slideshow_id i note]
  (as-> (get-slideshow-details query-fn slideshow_id) $
        (update $ :notes assoc i note)
        (slideshow-details query-fn slideshow_id $)))

(defn- move-up-v [v i]
  (if (pos? i)
    (-> v
        (assoc (dec i) (v i))
        (assoc i (v (dec i))))
    (conj (subvec v 1) (v 0))))
(defn- move-down-v [v i]
  (if (< i (dec (count v)))
    (-> v
        (assoc i (v (inc i)))
        (assoc (inc i) (v i)))
    (vec
     (conj (butlast v) (peek v)))))

(defn- del-v [v i]
  (vec
   (concat
    (subvec v 0 i)
    (subvec v (inc i)))))

(defn up-slideshow [query-fn slideshow_id i]
  (update-slides query-fn slideshow_id move-up-v i))
(defn down-slideshow [query-fn slideshow_id i]
  (update-slides query-fn slideshow_id move-down-v i))
(defn delete-slide [query-fn slideshow_id i]
  (update-slides query-fn slideshow_id del-v i)
  (delete/clean-files query-fn))

(defn copy-slide [query-fn slideshow_id i to-move]
  (let [{:keys [slides notes]} (get-slideshow-details query-fn slideshow_id)]
    (conj-slideshow-note query-fn to-move
                         (slides i)
                         (notes i))))

(defn jtd-images [query-fn slideshow_id]
  (->> (get-slideshow-slides query-fn slideshow_id)
       count
       range
       shuffle))

(defn jtd [query-fn slideshow_id images]
  (map (get-slideshow-slides query-fn slideshow_id) images))
(defn jtd-words [query-fn slideshow_id images]
  (map (get-slideshow-notes query-fn slideshow_id) images))

(defn get-slideshow-shuffled [query-fn slideshow_id order]
  (when-let [slides (not-empty (get-slideshow-slides query-fn slideshow_id))]
    (let [[i & rest] (or order (-> slides count range shuffle))]
      [(nth slides i) rest])))

(defn mobile [slideshow_id]
  (qr/generate-qr! (mobile/get-key slideshow_id)))

(defn mobile-random [query-fn key]
  (->> key mobile/key->slideshow_id (get-slideshow-slides query-fn) rand-nth))

(defn rot-slide [query-fn slideshow_id i]
  (let [local_id (-> (get-slideshow-slides query-fn slideshow_id) (get i) first)]
    (local/rotate-img local_id)
    local_id))
