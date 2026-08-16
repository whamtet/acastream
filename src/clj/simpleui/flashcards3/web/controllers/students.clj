(ns simpleui.flashcards3.web.controllers.students
  (:require
    [clojure.string :as string]
    [simpleui.flashcards3.web.controllers.students-persist :as students-persist]
    [simpleui.flashcards3.web.htmx :refer [page-htmx]]
    [simpleui.flashcards3.web.views.icons :as icons])
  (:import
    [java.text Normalizer Normalizer$Form]))

(defn- after [s split]
  (last (.split s split)))

(def r #"(?m)^\d+\s+(\D+)")

(defn q-header [question]
  [:th {:class "border border-gray-300 px-4 py-2 text-left font-semibold"}
   question])

(defn s-row [student colspan]
  [:tr
   [:td {:class "border border-gray-300 px-2 py-2 whitespace-nowrap"}
    student]
   [:td {:class "border border-gray-300 px-4 py-2"
         :colspan colspan}]])

(def space "&nbsp;&nbsp;")
(defn- smilies [stars]
  (if (= 3 stars)
    [icons/sad icons/neutral-face icons/smile]
    [icons/sad icons/smile]))

(defn- star-disp [stars icon]
  (case icon
    "tick" (list icons/check space icons/x-mark)
    "smiley" (interpose space (smilies stars))
    (interpose " " (repeat stars icons/star))))

(defn s-row2 [student colspan stars icon]
  [:tr
   [:td {:class "border border-gray-300 px-2 py-2 whitespace-nowrap"}
    student]
   (for [i (range colspan)]
     [:td {:class "border border-gray-300 px-4 py-2"}
      [:div.flex (star-disp stars icon)]])])

(defn- disp2 [updated questions students stars icon]
  [:div
   (when updated
     [:div {:class "print:hidden"} [:b.bold.mr-2 "Last updated:"] updated])
   [:table {:class "table-fixed min-w-full border border-gray-300"}
    [:colgroup
     [:col {:class "w-12"}]
     [:col {:class "w-auto" :span (count questions)}]]

    [:thead {:class "bg-gray-100"}
     [:tr
      [:th {:class "border border-gray-300 px-2 py-2 text-left font-semibold"}]
      (map q-header questions)]]

    [:tbody
     (map
      (if (or stars (not= "stars" icon))
        #(s-row2 % (count questions) stars icon)
        #(s-row % (count questions)))
      students)]]])

(defn- disp [updated questions students stars icon]
  (page-htmx
   {:css ["../output.css"]}
   (disp2 updated questions students stars icon)))

(defn- parse-questions [questions]
  (-> questions .trim (.split "\n")))

(defn- get-student [[_ student]]
  (let [student (.trim student)]
    (if (.startsWith student "New")
      (.substring student 4)
      student)))
(defn- get-students* [auth? students class]
  (if (and auth? (not-empty class))
    (students-persist/get-students class)
    (->> (after students "Note")
         (re-seq r)
         (map get-student))))

(defn remove-diacritics [s]
  (-> s
      (Normalizer/normalize Normalizer$Form/NFD)
      (clojure.string/replace #"\p{M}" "")
      (clojure.string/replace "Đ" "D")
      (clojure.string/replace "đ" "d")))

(defn get-students [students]
  (->> (after students "Note")
       remove-diacritics
       (re-seq r)
       (map get-student)))

(defn parse [{{:keys [questions students stars icon url class extra]} :params
              auth? :basic-authentication}]
  (let [students (get-students* auth? students class)
        stars (when (not-empty stars) (Long/parseLong stars))
        url (when url (.trim url))]
    (when (and auth? (not-empty url))
      (students-persist/assoc-students extra url students))
    (disp
     (when class (students-persist/get-updated class))
     (parse-questions questions)
     (concat students ["&nbsp;" "&nbsp;" "&nbsp;" "&nbsp;"])
     stars
     icon)))

