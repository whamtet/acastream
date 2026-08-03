(ns simpleui.flashcards3.web.views.play-video
  (:require
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.video :as video]
    [simpleui.flashcards3.web.views.components :refer [get-src2] :as components]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(defcomponent panel [req prefix ^:edn todo]
  (let [[head & rest] (or todo (video/video-info query-fn slideshow_id prefix))
        video? (and (string? head) (.endsWith head "webm"))]
    [:div {:hx-target "this"}
     (if video?
       [:video.fixed.inset-0.w-screen.h-screen.object-cover
        {:src "/cache/video/The_Floor_Is_Lava.1.webm"
         :autoplay true
         :playsinline true
         :hx-trigger "ended"
         :hx-post "panel"
         :hx-vals {:todo (pr-str rest)}}]
       "fuck")]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../../output.css"]}
      (-> req (assoc :query-fn query-fn) panel)))))
