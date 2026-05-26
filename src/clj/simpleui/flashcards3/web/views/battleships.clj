(ns simpleui.flashcards3.web.views.battleships
  (:require
    [clojure.string :as string]
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.views.components :as components]
    [simpleui.flashcards3.web.views.play-exchange :as play-exchange]
    [simpleui.flashcards3.web.controllers.pdf-battleships :as pdf-battleships]
    [simpleui.flashcards3.web.htmx :refer [page-htmx page-simple defcomponent]]))

(defn- parse-exclusions [x]
  (-> x .trim (.split "\n")))
(defcomponent ^:endpoint inner-names [req top1 top2]
  (let [exclusions (if (-> req :headers (get "hx-target") (= "top1"))
                     top2 top1)]
    (->> exclusions
         parse-exclusions
         (play-exchange/get-names 10)
         (string/join "\n"))))

(defcomponent form [req init]
  inner-names
  [:form#my-form
   {:class "p-2"
    :action "../pdf-battleships"
    :method "POST"}
   [:div.flex.items-center.py-2
    [:input {:type "submit"
             :value "Create"
             :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white mr-2"}]
    ;; alternate submit
    [:input {:type "submit"
             :formaction "../battleships-demo"
             :formmethod "GET"
             :value "Demo"
             :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white mr-2"}]]
   [:div.flex
    [:div.w-80.p-2
     [:div.text-xl.mb-2 "Left 1"]
     [:textarea {:class "border rounded-md p-2 w-full"
                 :rows 20
                 :name "left1"}
      init]]
    [:div.w-80.p-2
     [:div.flex.items-center.mb-2
      [:div.text-xl.mr-2 "Top 1"]
      [:div {:hx-post "inner-names"
             :hx-target "#top1"
             :hx-swap "innerHTML"
             :hx-include "#top2"}
       (components/button "Names")]]
     [:textarea {:class "border rounded-md p-2 w-full"
                 :rows 20
                 :name "top1"
                 :id "top1"}
      init]]]
   [:div.flex
    [:div.w-80.p-2
     [:div.text-xl.mb-2 "Left 2"]
     [:textarea {:class "border rounded-md p-2 w-full"
                 :rows 20
                 :name "left2"}
      init]]
    [:div.w-80.p-2
     [:div.flex.items-center.mb-2
      [:div.text-xl.mr-2 "Top 2"]
      [:div {:hx-post "inner-names"
             :hx-target "#top2"
             :hx-swap "innerHTML"
             :hx-include "#top1"}
       (components/button "Names")]]
     [:textarea {:class "border rounded-md p-2 w-full"
                 :rows 20
                 :name "top2"
                 :id "top2"}
      init]]]
   ])

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../output.css"]}
      (form req)))))

(defn- cell [x]
  [:div.border.text-center.text-2xl {:style {:height "22vh"}} x])

(defn demo [{:keys [query-string params]}]
  (let [[left top] (pdf-battleships/parse-demo params)
        src (format "pdf-battleships?%s&demo=true" query-string)]
    (page-simple
     {:css ["../output.css"]}
     [:div
      [:div.grid.grid-cols-5.grid-rows-4.m-8
       ;; top
       (cell "") (->> top (take 4) (map cell))
       (->> left (take 3) (map (fn [left] (list (cell left) (repeat 4 (cell ""))))))]
      [:div.flex
       [:iframe {:class "w-1/2"
                 :style {:height "1000px"}
                 :src src}]
       [:iframe {:class "w-1/2"
                 :style {:height "1000px"}
                 :src (str src 2)}]]])))
