(ns simpleui.flashcards3.web.views.grid
  (:require
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]
    [simpleui.flashcards3.web.views.components :refer [get-src] :as components]
    [simpleui.flashcards3.web.views.icons :as icons]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

;; this is the printout for writing words

(defn- drop-cycle [d cycle slides]
  (as-> slides $
        (drop d $)
        (repeat cycle $)
        (apply concat $)))

(defcomponent panel [req ^:long lines ^:long drop ^:long cycle]
  (let [slides (slideshow/get-slideshow-slides query-fn slideshow_id)]
    (if (empty? slides)
      "Empty"
      [:div.grid.grid-cols-2
       (for [[_ src] (drop-cycle (or drop 0) (or cycle 1) slides)]
         [:div.pb-3
          [:img {:src (get-src src)}]
          (repeat (or lines 1) [:hr {:class "mt-12 border border-black ml-3 w-4/5"}])])])))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../../output.css"]}
      (-> req (assoc :query-fn query-fn) panel)))))
