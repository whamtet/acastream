(ns simpleui.flashcards3.web.views.scramble-paragraph
    (:require
      [simpleui.core :as simpleui]
      [simpleui.flashcards3.web.controllers.reading :as reading]
      [simpleui.flashcards3.web.views.components :as components]
      [simpleui.flashcards3.web.views.icons :as icons]
      [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(defn- get-paragraphs [query-fn reading_id]
  (let [p (shuffle (reading/get-paragraphs query-fn reading_id))
        n (-> p count (* 0.5) long)]
    [(take n p)
     (drop n p)]))

(defn- paragraph [x]
  [:div.flex.items-center.justify-center.p-4.border
   [:p.fit.text-center.leading-tight
    x]])
(defn- count1 [x]
  (max (count x) 1))
(defn- row [items]
  [:div.grid {:style {:height "50vh" :grid-template-columns (format "repeat(%s, minmax(0, 1fr))" (count1 items))}}
   (map paragraph items)])

(defcomponent panel [req]
  (let [[a b] (get-paragraphs query-fn reading_id)]
    [:div.h-screen.flex.flex-col
     (row a)
     (row b)]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../../output.css"]
       :js ["../../scramble.js"]
       :fitty? true}
      (-> req (assoc :query-fn query-fn) panel)))))
