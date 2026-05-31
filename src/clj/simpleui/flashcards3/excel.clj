(ns simpleui.flashcards3.excel
  (:require
    [clojure.java.io :as io])
  (:import
    [java.io FileInputStream
     ByteArrayOutputStream
     ByteArrayInputStream]
    [org.apache.poi.xssf.usermodel XSSFWorkbook]))

(defn- get-or-create-row [sheet row-num]
  (or (.getRow sheet row-num)
      (.createRow sheet row-num)))

(defn- get-or-create-cell [row col-num]
  (or (.getCell row col-num)
      (.createCell row col-num)))

(defn- mi [f s]
  (dorun (map-indexed f s)))

(defn xlsx-stream
  [i j updates]
  (with-open [in (-> "kahoot.xlsx" io/resource io/input-stream)
              workbook (XSSFWorkbook. in)]
    (let [sheet (.getSheet workbook "Sheet1")
          out (ByteArrayOutputStream.)]
      (mi
       (fn [k row-data]
         (let [row (get-or-create-row sheet (+ i k))]
           (mi
            (fn [l cell-data]
              (.setCellValue (get-or-create-cell row (+ j l)) (str cell-data)))
            row-data)))
       updates)
      (.write workbook out)
      (ByteArrayInputStream. (.toByteArray out)))))
