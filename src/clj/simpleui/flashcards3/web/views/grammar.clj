(ns simpleui.flashcards3.web.views.grammar
  (:require
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.grammar :as grammar]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(def links
  [:div.m-2
   [:h1.text-3xl "Grammar Lessons"]
   (for [lesson grammar/lessons]
     [:div.flex.items-center.mt-2
      [:span.text-lg.mr-2 lesson " : "]
      [:a.text-clj-blue.mr-2 {:href (format "../grammar-lesson?lesson=%s&n=14" lesson)}
       "Worksheet"]
      [:span.mr-2 " | "]
      [:a.text-clj-blue.mr-2 {:href (format "../grammar-game?lesson=%s" lesson)}
       "Game"]])
   ])

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../output.css"]}
      links))))

(defcomponent lesson-ui [req lesson ^:long n]
  [:div
   (for [group (grammar/line-groups lesson n)]
     [:div.mb-6
      (for [line group]
        [:div line])])])

(defn ui-routes-lesson [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../output.css"]}
      (lesson-ui req)))))

(defn- paragraph [x]
  [:div.flex.items-center.justify-center.p-4.border
   [:p.fit.text-center.leading-tight
    x]])
(defn- count1 [x]
  (max (count x) 1))
(defn- row [items]
  [:div.grid {:style {:height "50vh"
                      :grid-template-columns (format "repeat(%s, minmax(0, 1fr))" (count1 items))}}
   (map paragraph items)])

(defcomponent ^:endpoint panel [req lesson ^:long i]
  (let [i (or i 0)]
    [:div {:hx-post "panel"
           :hx-vals {:i (+ i 3)
                     :lesson lesson}}
     (row (grammar/sentence-pair lesson i))
     (when post?
       [:script "fit()"])]))

(defn ui-routes-game [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../output.css"]
       :js ["../scramble.js"]
       :fitty? true}
      (panel req)))))
