(ns simpleui.flashcards3.web.views.edit
  (:require
    [clojure.string :as string]
    [simpleui.core :as simpleui]
    [simpleui.response :as response]
    [simpleui.flashcards3.web.controllers.img-search :as img-search]
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]
    [simpleui.flashcards3.web.controllers.local :as local]
    [simpleui.flashcards3.web.views.components :refer [get-src] :as components]
    [simpleui.flashcards3.web.views.icons :as icons]
    [simpleui.flashcards3.web.htmx :refer [page-htmx defcomponent]]
    [simpleui.flashcards3.util :as util]))

(defcomponent ^:endpoint name-editor [req new-name command]
  (case command
    "update" (when (not-empty new-name)
               (slideshow/update-slideshow-name query-fn slideshow_id new-name)
               nil)
    "delete" (do
               (slideshow/delete-slideshow query-fn slideshow_id)
               {:hx-redirect "../../home/"})
    (let [slideshow-name (slideshow/get-slideshow-name query-fn slideshow_id)
          notes (slideshow/get-slideshow-notes query-fn slideshow_id)
          phrases (string/join "%0A" notes)]
      [:div
       [:div.p-2.flex.items-center
        [:a.mr-2 {:href "../../home/"} icons/home]
        [:input {:class "p-2 rounded-md border mr-2"
                 :hx-post "name-editor:update"
                 :name "new-name"
                 :value slideshow-name}]
        [:div {:class "mr-2"
               :hx-delete "name-editor:delete"
               :hx-confirm (format "Delete %s?" slideshow-name)}
         (components/button-warning "Delete")]
        [:a {:class "mr-2"
             :href (format "../../api/pdf/%s" slideshow_id)
             :target "_blank"}
         (components/button "Download PDF")]
        [:a {:class "mr-2"
             :href (format "../../grid/%s/" slideshow_id)
             :target "_blank"}
         (components/button "Grid Printout")]
        [:a {:class "mr-2"
             :href (format "../../play-exchange/%s/" slideshow_id)
             :target "_blank"}
         (components/button "Exchange")]
        [:a {:class "mr-2"
             :href (str "../../snl/?init=" phrases)
             :target "_blank"}
         (components/button "SNL")]
        [:a {:class "mr-2"
             :href (str "../../blooket/?init=" phrases)
             :target "_blank"}
         (components/button "Blooklet")]
        [:a {:class "mr-2"
             :href (str "../../battleships/?init=" phrases)
             :target "_blank"}
         (components/button "Battleships")]
        [:a {:class "mr-2"
             :href (format "../../jtd/%s/" slideshow_id)
             :target "_blank"}
         (components/button "JTD")]
        [:div {:class "cursor-pointer"
               :hx-post "panel:duplicate"
               :hx-confirm "Duplicate?"}
         (components/button "Duplicate")]]
       [:div.p-2.flex.items-center
        [:a {:class "mr-2"
             :href (format "../../word-search/%s/" slideshow_id)
             :target "_blank"}
         (components/button "Word Search")]
        [:a {:class "mr-2"
             :href (format "../../memory/%s/" slideshow_id)
             :target "_blank"}
         (components/button "Memory")]
        [:a {:class "mr-2"
             :href (format "../../api/pdf-bingo/%s?pages=20&scale=0.17" slideshow_id)
             :target "_blank"}
         (components/button "Bingo")]
        [:a {:class "mr-2"
             :href (str "../../blooket/?dice=true&init=" phrases)
             :target "_blank"}
         (components/button "Dice PDF")]
        [:div {:class "cursor-pointer"
               :hx-post "panel:shuffle"
               :hx-confirm "Shuffle?"}
         (components/button "Shuffle")]]])))

(defn- get-src-simple [x]
  (if (string? x)
    x
    (format "../../local/%s" x)))

(defcomponent ^:endpoint image-note [req ^:long i note]
  (slideshow/slideshow-note query-fn slideshow_id i note)
  nil)

[:div {:class "w-2/3"}]
(defcomponent ^:endpoint move-modal [req ^:long i ^:long to-move]
  (if post?
    (do
      (slideshow/copy-slide query-fn slideshow_id i to-move)
      [:div#modal])
    (components/modal-scroll
     "w-2/3"
     [:div.p-2
      (for [{:keys [slideshow_id slideshow_name]} (slideshow/get-slideshows-summary query-fn)]
        [:div.p-2
         [:span.cursor-pointer.text-blue-500
          {:hx-post "move-modal"
           :hx-target "#modal"
           :hx-vals {:i i :to-move slideshow_id}}
          slideshow_name]])])))

(defcomponent ^:endpoint rot [req ^:long i]
  (let [medium (slideshow/rot-slide query-fn slideshow_id i)]
    [:img {:class "max-h-96"
           :hx-post "rot"
           :hx-trigger "contextmenu"
           :hx-vals {:i i}
           :oncontextmenu "event.preventDefault()"
           :src (str (get-src medium) "?x=" (rand)) ;; cache buster
           }]))

(defcomponent ^:endpoint image-order [req
                                      command
                                      medium
                                      large
                                      ^:long i
                                      ^:array images]
  image-note
  move-modal
  rot
  (case command
    "concat" (slideshow/concat-slideshow query-fn slideshow_id images)
    "concat2" (slideshow/concat-slideshow2 query-fn slideshow_id images)
    "conj" (slideshow/conj-slideshow query-fn slideshow_id [(or medium large) large])
    "up" (slideshow/up-slideshow query-fn slideshow_id i)
    "down" (slideshow/down-slideshow query-fn slideshow_id i)
    "del" (slideshow/delete-slide query-fn slideshow_id i)
    nil)
  [:div#images
   (when top-level? [:script "clipboardListener();"])
   [:div.flex.m-2
    [:form {:hx-post "image-order:concat2"
            :class "mr-2"
            :hx-encoding "multipart/form-data"
            :hx-target "#images"}
     (components/button "Paste" "pasteClipboard")
     [:input#clipboard.hidden {:type "file" :name "images"}]
     [:input#clipboardSubmit.hidden {:type "submit"}]]
    [:form {:hx-post "image-order:conj"
            :hx-target "#images"}
     [:input {:class "p-2 rounded-md border mr-2 w-96"
              :placeholder "Direct URL"
              :name "large"}]]
    [:input {:hx-post "image-order:concat"
             :hx-encoding "multipart/form-data"
             :hx-target "#images"
             :type "file"
             :accept (string/join ", " local/supported-types)
             :multiple true
             :name "images"}]]
   (map-indexed
    (fn [i [medium note]]
      [:div
       [:div.flex.items-center.mb-1
        [:span.text-xl.mr-1 (format "%s)" (inc i))]
        [:div {:class "cursor-pointer border rounded-md mr-2 p-2"
               :hx-post "image-order:up"
               :hx-target "#images"
               :hx-vals {:i i}}
         icons/arrow-up]
        [:div {:class "cursor-pointer border rounded-md mr-2 p-2"
               :hx-post "image-order:down"
               :hx-target "#images"
               :hx-vals {:i i}}
         icons/arrow-down]
        [:a {:class "mr-2"
             :tabindex -1
             :href (format "../../play/%s/%s/" slideshow_id i)}
         [:img {:class "max-h-96"
                :hx-post "rot"
                :hx-trigger "contextmenu"
                :hx-vals {:i i}
                :oncontextmenu "event.preventDefault()"
                :src (get-src medium)}]]
        [:div {:class "cursor-pointer border rounded-md p-2 mr-2"
               :hx-post "image-order:del"
               :hx-target "#images"
               :hx-confirm "Delete pic?"
               :hx-vals {:i i}}
         icons/trash]
        [:div {:class "cursor-pointer border rounded-md p-2"
               :hx-get "move-modal"
               :hx-vals {:i i}
               :hx-target "#modal"}
         icons/arrow-right]]
       [:input {:class "border rounded-md p-2 mt-1 mb-4 ml-20"
                :style {:width "500px"}
                :hx-post "image-note"
                :hx-vals {:i i}
                :value note
                :name "note"}]])
    (slideshow/get-slideshow-slides-edit query-fn slideshow_id))])

(defcomponent ^:endpoint image-search [req q]
  [:div {:class "p-2"
         :hx-target "this"}
   (when q
     [:div.flex.overflow-x-auto.mb-2
      (for [[medium large] (img-search/get-pics q)]
        [:img {:class "mr-1 cursor-pointer w-96"
               :src (get-src-simple medium)
               :hx-post "image-order:conj"
               :hx-target "#images"
               :hx-vals {:medium medium :large large}
               }])])
   [:form {:hx-get "image-search"}
    [:input {:class "p-2 rounded-md border"
             :placeholder "Search photos..."
             :name "q"}]]])

(defcomponent ^:endpoint icon-search [req q]
  [:div {:class "p-2"
         :hx-target "this"}
   (when q
     [:div.flex.overflow-x-auto.mb-2
      (for [[medium large] (img-search/get-icons q)]
        [:img {:class "mr-1 cursor-pointer"
               :src (get-src medium)
               :hx-post "image-order:conj"
               :hx-target "#images"
               :hx-vals {:medium medium :large large}
               }])])
   [:form {:hx-get "icon-search"}
    [:input {:class "p-2 rounded-md border"
             :placeholder "Search icons..."
             :name "q"}]]])

(defcomponent ^:endpoint panel [req ^:prompt slideshow-name command]
  (case command
    "duplicate"
    (response/hx-redirect
     (format "../%s/" (slideshow/duplicate-slideshow query-fn slideshow_id)))
    "shuffle"
    (do
      (slideshow/shuffle-slides query-fn slideshow_id)
      :refresh)
    [:div.p-2
     (name-editor req)
     [:hr.border-top]
     (image-order req)
     (image-search req)
     (icon-search req)]))

(defn ui-routes [{:keys [query-fn]}]
  (simpleui/make-routes
   ""
   [query-fn]
   (fn [req]
     (page-htmx
      {:css ["../../output.css"]
       :js ["../../edit.js"]}
      (-> req (assoc :query-fn query-fn) panel)))))
