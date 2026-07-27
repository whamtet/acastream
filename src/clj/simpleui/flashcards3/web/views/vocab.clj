(ns simpleui.flashcards3.web.views.vocab
  (:require
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(defcomponent vocabs [req]
  [:div.p-2.gap-2.flex.flex-col
   (for [{:keys [slideshow_id slideshow_name]} (slideshow/get-slideshows-flat query-fn)]
     [:div
      [:a.text-clj-blue {:href (format "../vocab/%s/" slideshow_id)} slideshow_name]])])

(defcomponent vocab [req]
  [:div.p-2.gap-2.flex.flex-col
   (map-indexed
    (fn [i x]
      [:div (inc i) ") " x])
    (slideshow/get-slideshow-notes query-fn slideshow_id))])

(defn ui-routes-vocabs [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../output.css"]}
      (-> req (assoc :query-fn query-fn) vocabs)))))

(defn ui-routes-vocab [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../../output.css"]}
      (-> req (assoc :query-fn query-fn) vocab)))))
