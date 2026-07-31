(ns simpleui.flashcards3.web.views.boggle
  (:require
    [simpleui.flashcards3.web.controllers.boggle :as boggle]
    [simpleui.flashcards3.web.htmx :refer [page-simple defcomponent]]))

[:div.flex.items-center.justify-center.w-16.h-16.rounded-lg.border.text-2xl.font-bold.shadow "T"]
(defn boggle-handler [_]
  (fn [req]
    (page-simple
     {:css ["../output.css"]
      :js ["../boggle.js"]}
     [:div.mt-4.flex.flex-col.items-center.gap-2
      [:h1#time-disp.text-3xl "2:00"]
      [:div.grid.grid-cols-4.gap-2.max-w-xs.mx-auto
       (map #(vector :div.flex.items-center.justify-center.w-16.h-16.rounded-lg.border.text-2xl.font-bold.shadow %)
            (boggle/result))]])))
