(ns simpleui.flashcards3.web.views.hours-total
  (:require
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.env :refer [prod?]]
    [simpleui.flashcards3.web.controllers.hours :as hours]
    [simpleui.flashcards3.web.views.components :as components]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(defn- table-row [[course count hours]]
  [:tr {:class "hover:bg-gray-50"}
   [:td {:class "px-4 py-2 text-sm text-gray-800"} course]
   [:td {:class "px-4 py-2 text-sm text-gray-800"} count]
   [:td {:class "px-4 py-2 text-sm text-gray-800"} hours]])

(defn- default [ym]
  (let [{:keys [table total]} (hours/ym-table ym)]
    [:div {:class "overflow-x-auto mt-4"}
     [:table {:class "min-w-full border border-gray-200 rounded-lg shadow-sm"}
      [:thead {:class "bg-gray-100"}
       [:tr
        [:th {:class "px-4 py-2 text-left text-sm font-semibold text-gray-700 border-b"}
         "Course Code"]
        [:th {:class "px-4 py-2 text-left text-sm font-semibold text-gray-700 border-b"}
         "Count"]
        [:th {:class "px-4 py-2 text-left text-sm font-semibold text-gray-700 border-b"}
         "Total Hours"]]]
      [:tbody {:class "divide-y divide-gray-200 bg-white"}
       (map table-row table)
       [:tr {:class "hover:bg-gray-50 font-semibold"}
        [:td {:class "px-4 py-2 text-sm text-gray-800"}]
        [:td {:class "px-4 py-2 text-sm text-gray-800"}]
        [:td {:class "px-4 py-2 text-sm text-gray-800"} total]]]]]))

(defn- table-row2 [[course count]]
  [:tr {:class "hover:bg-gray-50"}
   [:td {:class "px-4 py-2 text-sm text-gray-800"} course]
   [:td {:class "px-4 py-2 text-sm text-gray-800"} count]])

(defn- missing* [title rows]
  [:div {:class "overflow-x-auto mt-4"}
   [:h2.text-2xl title]
   [:table {:class "min-w-full border border-gray-200 rounded-lg shadow-sm"}
    [:thead {:class "bg-gray-100"}
     [:tr
      [:th {:class "px-4 py-2 text-left text-sm font-semibold text-gray-700 border-b"}
       "Course Code"]
      [:th {:class "px-4 py-2 text-left text-sm font-semibold text-gray-700 border-b"}
       "Count"]]]
    [:tbody {:class "divide-y divide-gray-200 bg-white"}
     (map table-row2 rows)]]])

(defn- missing [ym reported]
  (let [[a b] (hours/remainders ym reported)]
    [:div.mb-2
     (missing* "Recorded Excess" a)
     (missing* "VUS Excess" b)]))

(defcomponent ^:endpoint report [req ^:long year ^:long month reported]
  (let [current (hours/year-month)
        ym (if year (hours/year-month year month) current)
        previous (hours/dec-month ym)
        next (hours/inc-month ym)
        {:keys [table total]} (hours/ym-table ym)
        previous-href (format ".?year=%s&month=%s" (.getYear previous) (.getMonthValue previous))
        next-href (format ".?year=%s&month=%s" (.getYear next) (.getMonthValue next))]
    [:div.p-3 {:hx-target "this"}
     [:div.flex.items-center
      [:a {:href previous-href}
       (components/button "Previous Month")]
      ym
      (when (not= current ym)
        [:a {:href next-href}
         (components/button "Next Month")])]
     (if reported
       (missing ym reported)
       (default ym))
     [:form {:class "mt-2"
             :hx-post "report"
             :hx-vals {:year year :month month}}
      (components/submit "Analyze")
      [:div.mt-2.text-sm.text-gray-500 "Use CSV export (actually TSV)"]
      [:textarea {:class "w-full rounded-md border mt-2 p-2"
                  :style {:height "50vh"}
                  :required true
                  :placeholder "VUS Reported"
                  :name "reported"}
       reported]]]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../output.css"]}
      (report req)))))
