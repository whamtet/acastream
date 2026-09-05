(ns simpleui.flashcards3.web.views.videos
  (:require
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.videos :as videos]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]])
  (:import
    java.io.File))

(defcomponent page [req]
  [:div.p-2.gap-2.flex.flex-col
   (map
    (fn [^File x]
      [:div [:a.text-clj-blue {:href (format "/ncache/video/%s" (.getName x))} (.getName x)]])
    (videos/videos))])

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../output.css"]}
      (-> req (assoc :query-fn query-fn) page)))))
