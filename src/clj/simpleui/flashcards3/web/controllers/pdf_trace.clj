(ns simpleui.flashcards3.web.controllers.pdf-trace
  (:require
    [clj-pdf.core :as pdf]
    [simpleui.flashcards3.web.controllers.students :as students])
  (:import
    [java.io ByteArrayInputStream ByteArrayOutputStream]))

(defn- student-row [student]
  [:paragraph {:size 70} student])

(def margin 10)

(defn- pdf [students]
  (let [out (ByteArrayOutputStream.)]
    (pdf/pdf
     [{:font {:encoding :unicode
              :ttf-name "fonts/Trace-lxy0.ttf"}
       :orientation :landscape
       :footer false
       :left-margin   margin
       :right-margin  margin
       :top-margin    margin
       :bottom-margin margin}
      (map student-row students)]
     out)
    (-> out .toByteArray ByteArrayInputStream.)))

(defn get-pdf [{:keys [students]}]
  (->> students
       students/get-students
       pdf))
