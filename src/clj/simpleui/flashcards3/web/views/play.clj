(ns simpleui.flashcards3.web.views.play
  (:require
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]
    [simpleui.flashcards3.web.views.components :refer [get-src2] :as components]
    [simpleui.flashcards3.web.views.icons :as icons]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(defn- other-randoms [curr max]
  (let [x (range max)]
    (shuffle
     (concat
      (take curr x)
      (drop (inc curr) x)))))

(defcomponent panel [req ^:longs randoms]
  (let [slides (slideshow/get-slideshow-slides query-fn slideshow_id)
        last? (-> slides count dec (= step))
        next-href (if (or (empty? slides) last?)
                    (format "../../../edit/%s/" slideshow_id)
                    (format "../../../play/%s/%s/"
                            slideshow_id
                            (inc step)))
        edit-href (format "../../../edit/%s/" slideshow_id)
        [random & randoms] (if (empty? randoms)
                             (other-randoms step (count slides))
                             randoms)
        random-href (when (> (count slides) 1)
                      (format "../../../play/%s/%s/"
                              slideshow_id
                              random))]
    [:div#parent
     [:a#editLink.hidden {:href edit-href}]
     [:form.hidden {:hx-get random-href
                    :hx-target "#parent"}
      (for [random randoms]
        [:input {:name "randoms" :value random}])
      [:input#randomLink {:type "submit"}]]
     (if (empty? slides)
       [:a {:href next-href}
        [:div.p-6.text-xl "Empty"]]
       (let [[_ src] (nth slides step)]
         [:a#next {:href next-href}
          [:div.flex.justify-center.items-center
           [:img {:src (get-src2 src)
                  :style {:max-width "1000px"}}]]]))]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../../../output.css"]
       :js ["../../../random.js"]
       :hyperscript? true}
      (-> req (assoc :query-fn query-fn) panel)))))
