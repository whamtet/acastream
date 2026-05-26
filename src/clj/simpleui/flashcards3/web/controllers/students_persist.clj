(ns simpleui.flashcards3.web.controllers.students-persist
  (:require
    [java-time.api :as jt])
  (:import
    java.util.Date))

(def ^:private info (atom {}))

(defn- slurp-students []
  @info)

(defn- update-students [f & args]
  (apply swap! info f args))

(defn- course-name [s]
  (second
   (re-find #"course_name=([^&]+)" s)))

(defn- local-date-time [s]
  (when-let [match (re-find #"start_date_time=([^&]+)" s)]
    (->> match
         second
         (jt/local-date-time "ddMMyyyyHHmm")
         (jt/format "EEE HH:mm"))))

(defn- disp [extra s]
  (.trim
    (str extra " " (course-name s) " " (local-date-time s))))

(defn assoc-students [extra url students]
  (update-students assoc (disp extra url) {:students students :updated (Date.)}))

(defn get-classes []
  (keys (slurp-students)))

(defn get-students [class]
  (get-in (slurp-students) [class :students]))
(defn get-updated [class]
  (get-in (slurp-students) [class :updated]))

(defn delete-class [class]
  (update-students dissoc class))
