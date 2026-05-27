(ns simpleui.flashcards3.web.views.home
    (:require
      [simpleui.core :as simpleui]
      [simpleui.response :as response]
      [simpleui.flashcards3.env :refer [prod?]]
      [simpleui.flashcards3.web.controllers.slideshow :as slideshow]
      [simpleui.flashcards3.web.views.components :refer [get-src] :as components]
      [simpleui.flashcards3.web.views.icons :as icons]
      [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(defn- href [href]
  (if prod?
    {:href href :target "_blank"}
    {:href href}))

(defn- slideshow-disp [{:keys [slideshow_id slideshow_name]
                        {[[src]] :slides} :details}]
  [:div
   [:div.relative.group.inline-flex.items-center
    [:div.p-2.text-2xl.flex.items-center
     [:a.text-clj-blue.mr-2 {:href (format "../edit/%s/" slideshow_id)}
      slideshow_name]
     [:a.mr-2 (href (format "../play/%s/0/" slideshow_id))
      icons/play-circle]
     [:a.mr-2.text-yellow-500 (href (format "../play-double/%s/" slideshow_id))
      icons/play-circle]
     [:a.mr-2.text-red-500 (href (format "../play-write/%s/" slideshow_id))
      icons/play-circle]
     [:a.mr-2.text-purple-500 (href (format "../play-effect/%s/" slideshow_id))
      icons/play-circle]
     [:a.mr-2.text-green-500 (href (format "../memory/%s/" slideshow_id))
      icons/play-circle]
     [:a.mr-2.text-sky-400 (href (format "../play-guess/%s/" slideshow_id))
      icons/play-circle]]
    (when src
      [:img.absolute.w-20.hidden.group-hover:block
       {:style {:left "110%"}
        :src (get-src src)}])]])

(defmacro tabs-sesh [body]
  `(if ~'top-level?
    {:session (assoc (:session ~'req) :tab ~'tab)
     :body ~body}
    ~body))

(defcomponent ^:endpoint tabs-disp [req tab]
  (let [tab (or tab (-> req :session :tab) "Look")
        [slideshows tabs] (slideshow/get-slideshows query-fn tab)
        style #(if (= % tab)
                "px-4 py-2 border-b-2 border-blue-500 text-blue-600 font-medium cursor-pointer transition-colors"
                "px-4 py-2 border-b-2 border-transparent text-gray-600 hover:text-gray-900 hover:border-gray-300 cursor-pointer transition-colors")]
    (tabs-sesh
     [:div {:hx-target "this"
            :class "mt-2"}
      [:div {:class "flex border-b border-gray-200 text-sm"}
       (for [s tabs]
         [:a {:class (style s)
              :hx-get "tabs-disp"
              :hx-vals {:tab s}}
          s])]
      [:div.mt-2
       (map slideshow-disp slideshows)]])))

(defcomponent ^:endpoint panel [req ^:prompt slideshow-name command]
  (case command
    "new" (when slideshow-name
            (when-let [new-id (slideshow/add-slideshow query-fn slideshow-name)]
              (response/hx-redirect (format "../edit/%s/" new-id))))
    [:div.p-2
     [:div.flex.items-center.mb-1
      [:div {:class "my-1 mr-2"
             :id "new-slideshow"
             :hx-post "panel:new"
             :hx-prompt "New Slideshow Name"}
       (components/button "New Slideshow")]
      [:a {:class "my-1 mr-2"
           :href "../studentss/"
           :target "_blank"}
       (components/button "Students")]
      [:a {:class "my-1 mr-2"
           :href "../snl/"
           :target "_blank"}
       (components/button "SNL")]
      [:a {:class "my-1 mr-2"
           :href "../dominos/"
           :target "_blank"}
       (components/button "Dominos")]
      [:a {:class "my-1 mr-2"
           :href "../icon-search/"
           :target "_blank"}
       (components/button "Icon PDF")]
      [:a {:class "my-1 mr-2"
           :href "../fill/"
           :target "_blank"}
       (components/button "Fill")]
      [:a {:class "my-1 mr-2"
           :href "../hours/"
           :target "_blank"}
       (components/button "Hours")]
      [:a {:class "my-1 mr-2"
           :href "../phonics.png"
           :target "_blank"}
       (components/button "Phonics")]
      [:a {:class "my-1 mr-2"
           :href "../abc.jpg"
           :target "_blank"}
       (components/button "ABC")]
      [:a {:class "my-1 mr-2"
           :href "../upbeat.mp3"
           :target "_blank"}
       (components/button "Music")]
      [:a {:class "my-1 mr-2"
           :href "../readings"
           :target "_blank"}
       (components/button "Readings")]]
     [:div {:class "right-2 top-2 absolute"}
      [:a {:href "../white.html" :target "_blank"}
       (components/button "White Screen")]]
     (tabs-disp req)]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../output.css"]
       :js ["../home.js"]}
      (-> req (assoc :query-fn query-fn) panel)))))
