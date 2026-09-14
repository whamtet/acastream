(ns simpleui.flashcards3.web.controllers.hours
    (:require
      [java-time.api :as jt]
      [simpleui.flashcards3.web.controllers.hours.parse :as parse])
    (:import
    java.io.File
    java.util.Date
    java.time.YearMonth))

(def f (File. "hours.edn"))

(defn- slurp-hours []
  (if (.exists f)
    (-> f slurp read-string)
    {}))

(defn- spit-hours [x]
  (assert (map? x))
  (->> x pr-str (spit f)))

(defn- update-hours [f & args]
  (as-> (slurp-hours) $
        (apply f $ args)
        (spit-hours $)))

(defn- assoc-hours [m]
  (update-hours merge m))

(defn get-hours []
  (sort-by first (slurp-hours)))

(defn delete-hour [date]
  (update-hours dissoc date))

(defn parse-hours [s]
  (assoc-hours (parse/parse-hours* s)))

(def year-month jt/year-month)
(def inc-month #(jt/plus % (jt/months 1)))
(def dec-month #(jt/plus % (jt/months -1)))

(defn- ym->zdt [^YearMonth ym]
  (jt/zoned-date-time
   (.getYear ym)
   (.getMonthValue ym)
   24
   0
   0
   0
   0
   parse/tz))

(defn- jd->zdt [^Date d]
  (-> d jt/instant (jt/zoned-date-time "Asia/Bangkok")))

(defn- ym-frequencies [^YearMonth ym]
  (let [upper (ym->zdt ym)
        lower (dec-month upper)]
    (->> (get-hours)
         (map #(update % 0 jd->zdt))
         (drop-while #(-> % first (jt/< lower)))
         (take-while #(-> % first (jt/< upper)))
         (map second)
         frequencies)))

(defn ym-table [^YearMonth ym]
  (let [table
        (for [[course frequency] (ym-frequencies ym)]
          [course
           frequency
           (* frequency
              (if (.startsWith course "HK") 1.5 2))])]
    {:table table
     :total (->> table (map last) (apply +))}))

(defn- week-start [[jd]]
  (let [zdt (jd->zdt jd)
        to-subtract (-> zdt .getDayOfWeek .getValue dec)
        start (jt/minus zdt (jt/days to-subtract))]
    (jt/truncate-to start :days)))

(defn- week-disp [[start frequency]]
  (let [end (jt/plus start (jt/days 6))]
    [(str
      (jt/format "d MMM" start)
      " - "
      (jt/format "d MMM uuuu" end))
     frequency]))

(defn weeks []
  (->> (get-hours)
       (map week-start)
       frequencies
       (sort-by first)
       (map week-disp)))

(defn- parse-row [line]
  (let [cols (.split line "\t")
        course (first cols)
        hours (Double/parseDouble (last cols))]
    [course (if (.startsWith course "HK") (-> hours (* 2) (/ 3) long) (-> hours (* 0.5) long))]))

(defn- parse-vus-hours [s]
  (->> (.split s "\n")
       (drop-while #(not (.startsWith % "Course Code")))
       rest
       (take-while #(not (.startsWith % "Overall Sum")))
       (keep parse-row)
       (into {})))

(defn- subtract-frequencies [f1 f2]
  (for [[k v] f1
        :let [v2 (- v (f2 k 0))]
        :when (pos? v2)]
    [k v2]))

(defn remainders [ym s]
  (let [vus-reported (parse-vus-hours s)
        recorded (ym-frequencies ym)]
    [(subtract-frequencies recorded vus-reported)
     (subtract-frequencies vus-reported recorded)]))
