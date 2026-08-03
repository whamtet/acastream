(ns simpleui.flashcards3.web.views.scramble-sentence
  (:require
    [clojure.string :as string]
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.reading :as reading]
    [simpleui.flashcards3.web.views.components :as components]
    [simpleui.flashcards3.web.views.icons :as icons]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(defn- inc-i [i paragraphs]
  (mod (inc i) (count paragraphs)))

(defn- shuffle-sentence [sentence]
  (->> sentence (re-seq #"\S+") shuffle (string/join " ")))
(defn- shuffle-sentences [i paragraphs]
  (-> paragraphs
      (nth i)
      (.split "\n")
      (->> (map shuffle-sentence) shuffle)))

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

(defcomponent ^:endpoint panel [req ^:long i]
  (let [i (or i 0)
        paragraphs (reading/get-paragraphs query-fn reading_id)]
    [:div {:hx-post "panel"
           :hx-vals {:i (inc-i i paragraphs)}}
     (->> paragraphs
          (shuffle-sentences i)
          row)
     (when post?
       [:script "fit()"])]))

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
