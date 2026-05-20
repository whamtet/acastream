(ns simpleui.flashcards3.web.controllers.pdf-dice
  (:require
    [clojure.java.io :as io]
    [clj-pdf.core :as pdf])
  (:import [java.io File
            ByteArrayOutputStream ByteArrayInputStream]))

(def margin 18)

(def dice (-> "icons/dice.svg" io/resource io/input-stream slurp))

(defn- parse [^String s]
  (-> s
      .trim
      (.split "\n")
      cycle
      (->> (take 6))))

(defn- pdf-table [rows]
  (vec
   (concat
    [:pdf-table
     {:width-percent 100 :border false :spacing-before 32}
     [50 50]]
    rows)))

(defn- svg [i]
  [:svg {:translate [230 (+ 40 (* i 150))]
         :scale 0.05} dice])

(defn pdf [{:keys [part1 part2]}]
  (let [part1 (parse part1)
        part2 (parse part2)
        out (ByteArrayOutputStream.)]
    ;; produce PDF in another thread
    (pdf/pdf
         [{:size :a4
           :left-margin   margin
           :right-margin  margin
           :top-margin    margin
           :bottom-margin margin}
          (repeat 5
                  (pdf-table
                   (map
                    (fn [i p1 p2]
                      [[:pdf-cell (format "%s) %s" (inc i) p1)]
                       [:pdf-cell (format "%s) %s" (inc i) p2)]])
                    (range)
                    part1
                    part2)))
          (map svg (range 5))]
         out)
    (ByteArrayInputStream. (.toByteArray out))))
