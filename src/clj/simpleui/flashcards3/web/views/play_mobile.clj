(ns simpleui.flashcards3.web.views.play-mobile
  (:require
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]
    [simpleui.flashcards3.web.views.components :refer [get-src]]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(defcomponent ^:endpoint panel [req key]
  (let [[_ src] (slideshow/mobile-random query-fn key)]
    [:div.flex.items-center.justify-center.w-screen.h-screen.bg-gray-100
     {:hx-post "panel"
      :hx-vals {:key key}}
     [:img.max-w-full.max-h-full.object-contain
      {:src (get-src src)}]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../../output.css"]}
      (-> req (assoc :query-fn query-fn) panel)))))
