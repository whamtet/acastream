(ns simpleui.flashcards3.web.views.maze
  (:require
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.maze :as maze]
    [simpleui.flashcards3.web.views.icons :as icons]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(def border-style "1px solid black")
[:div.w-12.h-12]
(defn- cell [c x]
  [:div {:class "w-12 h-12 p-3 text-xl"
         :style (cond-> {}
                  (bit-test x 0) (assoc :border-top border-style)
                  (bit-test x 1) (assoc :border-right border-style)
                  (bit-test x 2) (assoc :border-bottom border-style)
                  (bit-test x 3) (assoc :border-left border-style))}
   (case c
     :start [:div.relative.bottom-10.right-3 (icons/arrow-down-width 12)]
     :end [:div.relative.top-5.right-3 (icons/arrow-down-width 12)]
     c)])

(defcomponent panel [req ^:long grid-size]
  (let [[placements maze] (maze/maze query-fn slideshow_id)]
    [:div.flex.justify-center
     [:div {:class "grid m-12"
            :style {:grid-template-rows (format "repeat(%s, minmax(0, 1fr))" maze/m)
                    :grid-template-columns (format "repeat(%s, minmax(0, 1fr))" maze/n)}}
      (map cell placements maze)]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../../output.css"]}
      (-> req (assoc :query-fn query-fn) panel)))))
