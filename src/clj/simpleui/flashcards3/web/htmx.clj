(ns simpleui.flashcards3.web.htmx
  (:require
   [hiccup.core :as h]
   [hiccup.page :as p]
   [ring.util.http-response :as http-response]
   [simpleui.core :as simpleui]
   [simpleui.flashcards3.env :refer [dev?]]
   [simpleui.flashcards3.web.resource-cache :as resource-cache]
   [simpleui.render :as render]))

(defn page [opts & content]
  (-> (p/html5 opts content)
      http-response/ok
      (http-response/content-type "text/html")))

(defn ui [opts & content]
  (-> (h/html opts content)
      http-response/ok
      (http-response/content-type "text/html")))

(defn- unminify [^String s]
  (if dev?
    (.replace s ".min" "")
    s))

(defn- scripts [js {:keys [hyperscript? fitty?]}]
  (cond-> (map resource-cache/cache-suffix js)
          hyperscript? (conj (unminify "https://unpkg.com/hyperscript.org@0.9.12/dist/_hyperscript.min.js"))
          fitty? (conj "https://cdn.jsdelivr.net/npm/fitty@2.3.6/dist/fitty.min.js")))

(defn page-htmx [{:keys [css js] :as opts} & body]
  (page
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0"}]
    [:title "SimpleUI Flashcards"]
    [:link {:rel "icon" :href "/logo_dark.svg"}]
    (for [sheet css]
      [:link {:rel "stylesheet" :href (resource-cache/cache-suffix sheet)}])]
   [:body
    [:div#modal.hidden]
    (render/walk-attrs body)
    (map
     (fn [src]
       [:script {:src src}])
     (scripts js opts))
    [:script {:src
              (unminify "https://unpkg.com/htmx.org@1.9.5/dist/htmx.min.js")}]
    [:script "htmx.config.defaultSwapStyle = 'outerHTML';"]
    #_#_
    [:script {:src "https://cdn.jsdelivr.net/npm/eruda"}]
    [:script "eruda.init();"]]))

(defn page-simple [{:keys [css js] :as opts} & body]
  (page
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0"}]
    (for [sheet css]
      [:link {:rel "stylesheet" :href (resource-cache/cache-suffix sheet)}])]
   [:body (render/walk-attrs body)]
   (map
    (fn [src]
      [:script {:src src}])
    (scripts js opts))))

(def optionals
  '{slideshow_id [slideshow_id (Long/parseLong (:slideshow_id params))]
    reading_id [reading_id (some-> params :reading_id Long/parseLong)]
    step [step (Long/parseLong (:step params))]
    post? [post? (simpleui/post? req)]})

(defmacro defcomponent
  [name [req :as args] & body]
  (if-let [sym (simpleui/symbol-or-as req)]
    `(simpleui/defcomponent ~name ~args
      (let [{:keys [~'path-params ~'query-fn]} ~sym
            ~'params (merge ~'params ~'path-params)
            ~@(->> body flatten (mapcat optionals) distinct)]
        ~@body))
    (throw (Exception. "req ill defined"))))
