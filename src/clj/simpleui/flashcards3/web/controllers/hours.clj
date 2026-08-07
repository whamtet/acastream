(ns simpleui.flashcards3.web.controllers.hours
  (:require
    [java-time.api :as jt])
  (:import
    java.io.File
    java.util.Date
    java.time.YearMonth))

(def f (File. "hours.edn"))
(def tz "Asia/Ho_Chi_Minh")

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

(def days ["Monday" "Tuesday" "Wednesday" "Thursday" "Friday" "Saturday" "Sunday"])
(defn- day-line? [line]
  (some
   #(when (.startsWith line %)
     (-> line (.replace % "") .trim))
   days))

(def time-regex #"(\d{1,2}:\d{2})\s*-\s*\d{1,2}:\d{2}\s+([A-Za-z0-9-]+)")

(defn- get-date [date-str start-time]
  (->
   (jt/local-date-time "d MMMM yyyy H:mm" (str date-str " " start-time))
   (jt/zoned-date-time tz)
   jt/java-date))

(defn parse-hours* [s]
  (loop [[line & todo] (-> s .trim (.split "\n"))
         date nil
         done {}]
    (if line
      (let [line (.trim line)]
        (if-let [date-str (day-line? line)]
          (recur todo date-str done)
          (if-let [[_ start-time class] (re-find time-regex line)]
            (->> class (assoc done (get-date date start-time)) (recur todo date))
            (recur todo date done))))
      done)))

(defn parse-hours [s]
  (assoc-hours (parse-hours* s)))

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
   tz))

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
  (when (re-find #"^\d" line)
    (let [cols (.split line "\t")]
      [(second cols)
       (Long/parseLong (nth cols (- (count cols) 2)))])))

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
