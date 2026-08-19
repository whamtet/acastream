(ns simpleui.flashcards3.web.views.maze
  (:require
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.maze :as maze]
    [simpleui.flashcards3.web.views.components :as components]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(defcomponent panel [req ^:long grid-size]
  (let [_ (maze/maze query-fn slideshow_id)]
    [:div
     [:div {:class "grid border"
            :style {:grid-template-rows (format "repeat(%s, minmax(0, 1fr))" maze/m)
                    :grid-template-columns (format "repeat(%s, minmax(0, 1fr))" maze/n)}}]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../../output.css"]}
      (-> req (assoc :query-fn query-fn) panel)))))
