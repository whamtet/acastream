(ns simpleui.flashcards3.web.views.share
  (:require
    [clojure.string :as string]
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.qr :as qr]
    [simpleui.flashcards3.web.controllers.share :as share]
    [simpleui.flashcards3.web.controllers.local :as local]
    [simpleui.flashcards3.web.views.components :as components]
    [simpleui.flashcards3.web.htmx :refer [page-htmx page-simple defcomponent]]
    [simpleui.flashcards3.util :as util]))

(defcomponent ^:endpoint panel [req submission-name ^:array files command]
  (case command
    "submit" (do
               (share/add-entry submission-name files)
               [:div.p-4 "Thank you"])
    [:form.max-w-lg.mx-auto.p-4.space-y-4
     {:hx-post "panel:submit"
      :hx-encoding "multipart/form-data"}
     ;; Row 1: File selector
     [:div
      [:label.block.mb-2.text-sm.font-medium
       {:for "files"}
       "Select file(s)"]
      [:input.block.w-full.text-sm.text-gray-900
       {:id "files"
        :type "file"
        :name "files"
        :multiple true
        :required true
        :accept (when (share/force-images?) (string/join ", " local/supported-types))
        :class "block w-full text-sm
             file:mr-4 file:rounded-md file:border-0
             file:bg-blue-600 file:px-4 file:py-2
             file:text-white hover:file:bg-blue-700
             cursor-pointer"}]]

     ;; Row 2: Submission name
     [:div
      [:label.block.mb-2.text-sm.font-medium
       {:for "submission-name"}
       "Submission name"]
      [:input.w-full.rounded-md.border.border-gray-300.px-3.py-2
       {:id "submission-name"
        :type "text"
        :name "submission-name"
        :placeholder "Enter a name"
        :required true}]]

     ;; Row 3: Submit button
     [:div
      [:button.w-full.rounded-md.bg-blue-600.px-4.py-3.font-medium.text-white
       {:type "submit"}
       "Submit"]]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../output.css"]}
      (-> req (assoc :query-fn query-fn) panel)))))

(defn ui-routes-qr [_]
  (fn [req]
    (page-simple
     {:css ["../output.css"]}
     [:div.mt-4
      [:img {:class "mx-auto w-[500px]"
             :src (qr/base64 "/share/")}]])))
