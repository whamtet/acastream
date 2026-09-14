(ns simpleui.flashcards3.web.controllers.hours.parse
  (:require
    [java-time.api :as jt]))

(def tz "Asia/Ho_Chi_Minh")

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
