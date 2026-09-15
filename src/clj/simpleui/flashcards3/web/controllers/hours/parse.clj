(ns simpleui.flashcards3.web.controllers.hours.parse
  (:require
    [clojure.string :as string]
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

(defn- find-lines [todo k]
  (->> todo
       (drop-while #(not (.contains % k)))
       rest
       (take-while #(-> % .trim not-empty))
       (string/join "\n")
       .trim))

(defn- join-lines [line todo class]
  {:class class
   :location (second (.split line "Room: "))
   :book (find-lines todo "Book Name")
   :plan (find-lines todo "Lesson plan")})

(defn parse-hours-full [s]
  (loop [[line & todo] (-> s .trim (.split "\n"))
         date nil
         done {}]
    (if line
      (let [line (.trim line)]
        (if-let [date-str (day-line? line)]
          (recur todo date-str done)
          (if-let [[_ start-time class] (re-find time-regex line)]
            (->> class (join-lines line todo) (assoc done (get-date date start-time)) (recur todo date))
            (recur todo date done))))
      done)))
