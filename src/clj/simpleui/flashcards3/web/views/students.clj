(ns simpleui.flashcards3.web.views.students
  (:require
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.students-persist :as students-persist]
    [simpleui.flashcards3.web.views.components :as components]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(defn- quick-submit [class]
  (format "on click set #class.value to '%s' then call #my-form.submit()
  on contextmenu halt the event then set #class.value to '%s' then call #del.click()" class class))

(def radio
  [:div {:class "flex flex-row items-center gap-6"}
   [:label {:class "inline-flex items-center gap-2 cursor-pointer"}
    [:input {:type "radio"
             :name "icon"
             :value "stars"
             :checked true
             :class "h-4 w-4 text-blue-600 border-gray-300 focus:ring-blue-500"}]
    [:span {:class "text-gray-700"} "Stars"]]

   [:label {:class "inline-flex items-center gap-2 cursor-pointer"}
    [:input {:type "radio"
             :name "icon"
             :value "tick"
             :class "h-4 w-4 text-blue-600 border-gray-300 focus:ring-blue-500"}]
    [:span {:class "text-gray-700"} "Tick"]]

   [:label {:class "inline-flex items-center gap-2 cursor-pointer"}
    [:input {:type "radio"
             :name "icon"
             :value "smiley"
             :class "h-4 w-4 text-blue-600 border-gray-300 focus:ring-blue-500"}]
    [:span {:class "text-gray-700"} "Smiley"]]])

(defcomponent ^:endpoint form [req command class]
  (when (:basic-authentication req)
    (case command
      "del" (students-persist/delete-class class)
      nil))
  [:form#my-form
   {:class "p-2"
    :action (if (:basic-authentication req) "../api/studentss" "../api/students")
    :method "POST"
    :hx-target "this"}
   [:div.flex.items-center.p-2
    [:input {:type "submit"
             :id "parse"
             :value "Parse"
             :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white mr-2"}]
    [:input {:class "rounded-md border p-2 mr-4"
             :name "stars"
             :placeholder "Quantity (optional)"}]
    radio
    [:input
     {:type "submit"
      :value "Trace Names"
      :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white ml-2"
      :formaction "../pdf-trace"}]]
   [:textarea {:class "w-full rounded-md border mt-2 p-2"
               :style {:height "30vh"}
               :placeholder "Questions - one per line"
               :name "questions"}]
   (when (:basic-authentication req)
     (let [classes (students-persist/get-classes)]
       [:div
        [:div#del.hidden {:hx-delete "form:del" :hx-include "#class"}]
        [:div.flex.mt-2.gap-2
         (for [class classes]
           [:div {:_ (quick-submit class)} (components/button class)])]
        [:input {:class "rounded-md border mt-2 p-2 w-full"
                 :name "extra"
                 :placeholder "Extra Class Info"}]
        [:input {:class "rounded-md border mt-2 p-2 w-full"
                 :name "url"
                 :placeholder "URL"}]
        [:input#class {:type "hidden" :name "class"}]]))
   [:textarea {:class "w-full rounded-md border mt-2 p-2"
               :style {:height "60vh"}
               :placeholder "Students - copy from VUS attendance form"
               :name "students"}]])

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../output.css"]
       :hyperscript? (:basic-authentication req)}
      (form req)))))
