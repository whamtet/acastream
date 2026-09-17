(ns simpleui.flashcards3.web.views.fill
  (:require
    [clojure.string :as string]
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.env :refer [prod?]]
    [simpleui.flashcards3.web.views.components :as components]
    [simpleui.flashcards3.web.controllers.reading :as reading]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(def extension 2)

(defn- subtract-extension [x ending]
  (if (< x (+ ending extension))
    (min x ending)
    (- x extension)))

(defn- append-offset* [offsets i1 i2]
  (loop [[[t1 t2] & todo] (partition 2 offsets)
         i1 i1
         i2 i2]
    (if t1 ;; still have to check
      (let [i2 (if (<= i2 t2)
                 ;; move top down
                 (min i2 (dec t1))
                 i2)
            i1 (if (<= t1 i1)
                 ;; move bottom up
                 (max i1 (inc t2))
                 i1)]
        (recur todo i1 i2))
      (if (< i1 i2)
        (conj offsets i2 i1)
        offsets))))

(defn- append-offset [offsets i1 i2]
  (let [endings (->> offsets rest (take-nth 2) sort reverse)]
    (append-offset*
     offsets
     (reduce subtract-extension i1 endings)
     (reduce subtract-extension i2 endings))))

(defn- drop-offset [offsets]
  (drop 2 offsets))

(def superscripts ["¹" "²" "³" "⁴" "⁵" "⁶" "⁷" "⁸" "⁹" "¹⁰"
                   "¹¹" "¹²" "¹³" "¹⁴" "¹⁵" "¹⁶" "¹⁷" "¹⁸" "¹⁹"])

(defn- _ [superscript i]
  (apply str superscript (repeat i "_")))

(defn- white-out-string [s offsets]
  (let [sorted (->> offsets (partition 2) (sort-by first) (apply concat))
        padded (concat [0] sorted [(count s)])]
    (string/join
     (map
      (fn [i a b]
        (when (< a b)
          (if (even? i)
            (.substring s a b)
            (let [n (-> i (bit-shift-right 1) superscripts)]
              (_ n (- (+ b extension) a (count n)))))))
      (range)
      padded
      (rest padded)))))

(defn- bind [a b]
  (when a
    (-> a (max 0) (min b))))

(defcomponent ^:endpoint select [req text ^:longs offsets ^:long i1 ^:long i2 command]
  (let [num-offsets (-> offsets count (* 0.5) long)
        extended-text (+ (count text) (* extension num-offsets))
        i1 (bind i1 extended-text)
        i2 (bind i2 extended-text)
        offsets
        (case command
          "append" (append-offset offsets i1 i2)
          "drop" (drop-offset offsets)
          offsets)]
    [:div.p-2 {:hx-target "this"}
     [:form {:class "flex items-center mb-2 print:hidden"
             :hx-post "edit"
             :hx-confirm (when prod? "Back to Edit?")}
      (components/hiddens "text" text)
      (components/submit "Edit")
      [:div.text-gray-500.ml-4 "Drag over words to blank them out (max 9 words).  Press z to undo."]]
     [:form.hidden {:hx-post "select:append"}
      (components/hiddens "text" text
                          "offsets" offsets
                          "i1" "id"
                          "i2" "id")
      (components/submit-hidden "append")]
     [:form.hidden {:hx-post "select:drop"}
      (components/hiddens "text" text
                          "offsets" offsets)
      (components/submit-hidden "drop")]
     (repeat 10
             (list
              [:pre#text-disp.whitespace-pre-wrap
               {:style "font-size: 1.5em"}
               (white-out-string text offsets)]
              [:hr.my-8]))
     [:script "listenTextDisp();"]]))

(defcomponent ^:endpoint edit [req text]
  select
  [:form {:class "p-2"
          :hx-post "select"}
   (components/submit "Highlight")
   [:textarea {:class "w-full rounded-md border mt-2 p-2 update"
               :style {:height "80vh"}
               :required true
               :name "text"}
    (or text
        (when (and (:basic-authentication req) reading_id)
          (reading/get-details query-fn reading_id)))]])

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (let [prefix (when (:basic-authentication req) "../")]
       (page-htmx
        {:css [(str prefix "../output.css")]
         :js [(str prefix "../fill.js")]}
        (-> req (assoc :query-fn query-fn) edit))))))
