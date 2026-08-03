(ns simpleui.flashcards3.web.views.play-exchange
  (:require
    [clojure.java.io :as io]
    [clojure.set :as set]
    [simpleui.core :as simpleui]
    [simpleui.flashcards3.web.views.components :refer [get-src]]
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]))

(def ten (-> "ten.txt" io/resource slurp .trim (.split "\n") seq))

(defn get-names [n exclusions]
  (->> exclusions
       set
       (set/difference (set ten))
       shuffle
       (take n)))

(defn- square [title [src2 src]]
  [:div.relative.border
   [:div.absolute.left-2.top-2.text-2xl title]
   (when src
     [:a {:href (if (number? title) ".?names=true" ".?names=false")}
      [:img {:src (get-src src)}]])])

(defn- page [cols images names]
  [:div {:class "print-landscape grid grid-rows-2"
         :style {:grid-template-columns (format "repeat(%s, minmax(0, 1fr))" cols)}}
   (map square (or names (map inc (range))) images)])

(defcomponent pages [req ^:boolean names]
  (let [slides (slideshow/get-slideshow-slides query-fn slideshow_id)
        cols (-> slides count (/ 2) Math/ceil long)
        nils (map (constantly nil) slides)
        [names1 names2] (when names [(shuffle ten) (shuffle ten)])]
    [:div
     (page cols (shuffle slides) names1)
     (page cols nils names2)
     (page cols (shuffle slides) names2)
     (page cols nils names1)
     ]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../../output.css"]}
      (-> req (assoc :query-fn query-fn) pages)))))
