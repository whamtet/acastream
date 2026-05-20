(ns simpleui.flashcards3.web.views.blooket
  (:require
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.views.components :as components]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(defcomponent form [req init ^:boolean dice]
  [:form#my-form
   {:class "p-2"
    :action (if dice "../pdf-dice" "../blooket-csv")
    :method "POST"}
   [:div.flex.items-center.py-2
    [:input {:type "submit"
             :value "Create"
             :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white mr-2"}]]
   [:div.flex
    [:div.p-2
     [:div.text-xl.mb-2 (if dice "Part 1" "Questions (one per line)")]
     [:textarea {:class "border rounded-md p-2 resize"
                 :style {:min-width "500px"}
                 :rows 20
                 :name (if dice "part1" "questions")}]]
    [:div.p-2
     [:div.text-xl.mb-2 (if dice "Part 2" "Answers (one per line)")]
     [:textarea {:class "border rounded-md p-2 resize"
                 :style {:min-width "300px"}
                 :rows 20
                 :name (if dice "part2" "answers")}
      init]]]
   ])

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../output.css"]}
      (form req)))))
