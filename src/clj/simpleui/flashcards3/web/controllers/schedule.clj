(ns simpleui.flashcards3.web.controllers.schedule
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [simpleui.flashcards3.web.controllers.hours.parse :as hours.parse])
  (:import
    (java.util Date)
    (java.time ZoneOffset Instant)
    (java.time.format DateTimeFormatter)))

(def ^:private cal-text (-> "cal.txt" io/resource slurp))

(def utc-formatter
  (DateTimeFormatter/ofPattern "yyyyMMdd'T'HHmmss'Z'"))

(defn format-ics-date [^Date date]
  (let [instant (.toInstant date)
        zdt     (.atZone instant ZoneOffset/UTC)]
    (.format utc-formatter zdt)))

(defn add-minutes [^Date date minutes]
  (-> (.toInstant date)
      (.plusSeconds (* minutes 60))
      (Date/from)))

(def this-week (atom {}))

(defn update-hours [s]
  (reset! this-week (hours.parse/parse-hours-full s)))

(def LIMIT 75)
(defn ical-text [prefix s]
  (let [s (-> s
              (string/replace "\\" "\\\\")
              (string/replace ";" "\\;")
              (string/replace "," "\\,")
              (string/replace "\r\n" "\\n")
              (string/replace "\n" "\\n")
              (string/replace "\r" "\\n"))
        bytes #(count (.getBytes ^String % "UTF-8"))]
    (loop [chars (seq s)
           lines []
           line (str prefix ":")]
      (if-let [ch (first chars)]
        (let [candidate (str line ch)]
          (if (<= (bytes candidate) LIMIT)
            (recur (next chars) lines candidate)
            (recur (next chars)
              (conj lines line)
              (str " " ch))))
        (string/join "\r\n" (conj lines line))))))

(defn- pr-event** [[a b]]
  (ical-text a (if (instance? Date b) (format-ics-date b) b)))
(defn- pr-event* [& pairs]
  (->> pairs
       (partition 2)
       (map pr-event**)
       (string/join "\r\n")))

(defn- minutes [class]
  (cond
    (.startsWith class "HK") 90
    (.startsWith class "SK") 110
    :else 120))

(defn- pr-event [[start {:keys [class location book plan]} :as v]]
  (pr-event*
   "BEGIN" "VEVENT"
   "UID" (format "event%s@flashcards.simpleui.io" (hash v))
   "DTSTAMP" (Date.)
   "DTSTART" start
   "DTEND" (add-minutes start (minutes class))
   "SUMMARY" (str class " " location)
   "DESCRIPTION" (str book "\n\n" plan)
   "LOCATION" location
   "STATUS" "CONFIRMED"
   "SEQUENCE" 0
   "END" "VEVENT"
   ))

(defn cal-body []
  (->> @this-week
       (map pr-event)
       (string/join "\n")
       (format cal-text)))
