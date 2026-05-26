(ns simpleui.flashcards3.web.views.play-guess
  (:require
    [clojure.string :as string]
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(defn hide-vowels [s]
  (string/replace s #"[aeiou]" "_"))

(defn hide-consonants [s]
  (string/replace s #"[^aeiou]" "_"))

(defn scramble [s]
  (-> s seq shuffle string/join))

(defn create-guess [s]
  ((rand-nth [hide-vowels hide-consonants scramble]) s))

(defcomponent ^:endpoint panel [req ^:longs randoms]
  (let [notes (slideshow/get-slideshow-notes query-fn slideshow_id)]
    [:div {:style {:height "100vh"}
           :class "flex items-center justify-center"
           :hx-post "panel"}
     [:span.text-9xl (-> notes rand-nth create-guess)]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../../../output.css"]}
      (-> req (assoc :query-fn query-fn) panel)))))
