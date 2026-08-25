(ns simpleui.flashcards3.web.controllers.schedule
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [simpleui.flashcards3.web.controllers.hours :as hours])
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
      (.plusMinutes minutes)
      (Date/from)))

(def this-week (atom {}))

(defn update-hours [s]
  (reset! this-week (hours/parse-hours* s)))

(defn- pr-event** [[a b]]
  (str a ":" (if (instance? Date b) (format-ics-date b) b)))
(defn- pr-event* [& pairs]
  (->> pairs
       (partition 2)
       (map pr-event**)
       (string/join "\n")))

(defn- pr-event [[start class :as v]]
  (pr-event*
   "BEGIN" "VEVENT"
   "UID" (format "event%s@flashcards.simpleui.io" (hash v))
   "DTSTAMP" (Date.)
   "DTSTART" start
   "DTEND" (add-minutes start (if (.startsWith class "HK") 90 120))
   "SUMMARY" class
   "DESCRIPTION" class
   "LOCATION" class
   "STATUS" "CONFIRMED"
   "SEQUENCE" 0
   "END" "VEVENT"
   ))

(defn cal-body []
  (->> @this-week
       (map pr-event)
       (string/join "\n")
       (format cal-text)))
