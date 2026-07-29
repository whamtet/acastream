(ns simpleui.flashcards3.web.views.share-admin
  (:require
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.share :as share]
    [simpleui.flashcards3.web.views.components :as components]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]
    [simpleui.flashcards3.util :as util]))

(defcomponent ^:endpoint panel [req ^:long status ^:boolean force command]
  (case command
    "toggle" (share/toggle-open (pos? status))
    "delete" (do (share/delete-files) :refresh)
    "images" (share/force-images force)
    [:div.p-2
     [:div.flex.items-center.gap-3.mt-2.ml-2
      [:span.text-sm.font-medium "Closed"]
      [:input.accent-blue-600
       {:type "range"
        :hx-post "panel:toggle"
        :name "status"
        :min 0
        :max 1
        :step 1
        :value (if (share/open?) 1 0)}]
      [:span.text-sm.font-medium "Open"]]
     [:div.flex.items-center.gap-3.mt-2.ml-2
      [:span.text-sm.font-medium "Force Images?"]
      [:input.accent-blue-600
       {:type "checkbox"
        :hx-post "panel:images"
        :name "force"
        :checked (share/force-images?)}]]
     [:div.mt-3
      {:hx-post "panel:delete"
       :hx-confirm "Wipe Files?"}
      (components/button "Delete Files")]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../output.css"]}
      (-> req (assoc :query-fn query-fn) panel)))))
