(ns simpleui.flashcards3.web.views.play-guess
  (:require
    [clojure.string :as string]
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(defn- scramble [s]
  (-> s seq shuffle string/join))

(defn- paragraph [x]
  [:div {:class "text-center leading-tight"
         :style {:font-size "24px"}}
   [:div {:class "flex justify-center mb-6"}
    (for [c x]
      [:span {:class "inline-block w-8"} c])]
   [:div {:class "flex justify-center"}
    (for [_ x]
      [:span {:class "inline-block w-8"} "_"])]])

(defn- count1 [x]
  (max (count x) 1))
(defn- row [items]
  [:div.grid.mt-20
   {:style {:height "50vh"
            :grid-template-columns (format "repeat(%s, minmax(0, 1fr))" (count1 items))}}
   (map paragraph items)])

(defcomponent ^:endpoint panel [req ^:boolean random]
  (let [note (slideshow/get-slideshow-note query-fn slideshow_id)
        f (if random scramble identity)]
    [:div {:hx-post "panel"
           :hx-vals {:random random}}
     (row [(f note) (f note)])]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../../output.css"]}
      (-> req (assoc :query-fn query-fn) panel)))))
