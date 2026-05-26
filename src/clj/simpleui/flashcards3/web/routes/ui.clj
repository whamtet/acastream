(ns simpleui.flashcards3.web.routes.ui
  (:require
    [simpleui.flashcards3.web.middleware.auth :as auth]
    [simpleui.flashcards3.web.middleware.exception :as exception]
    [simpleui.flashcards3.web.middleware.formats :as formats]
    [simpleui.flashcards3.web.views.edit :as edit]
    [simpleui.flashcards3.web.views.grid :as grid]
    [simpleui.flashcards3.web.views.home :as home]
    [simpleui.flashcards3.web.views.readings :as readings]
    [simpleui.flashcards3.web.views.hours :as hours]
    [simpleui.flashcards3.web.views.hours-total :as hours-total]
    [simpleui.flashcards3.web.views.jtd :as jtd]
    [simpleui.flashcards3.web.views.play :as play]
    [simpleui.flashcards3.web.views.play-double :as play-double]
    [simpleui.flashcards3.web.views.play-effect :as play-effect]
    [simpleui.flashcards3.web.views.play-exchange :as play-exchange]
    [simpleui.flashcards3.web.views.play-guess :as play-guess]
    [simpleui.flashcards3.web.views.play-write :as play-write]
    [simpleui.flashcards3.web.views.fill :as fill]
    [simpleui.flashcards3.web.views.scramble-paragraph :as scramble-paragraph]
    [simpleui.flashcards3.web.views.scramble-sentence :as scramble-sentence]
    [simpleui.flashcards3.web.views.students :as students]
    [simpleui.flashcards3.web.views.word-search :as word-search]
    [simpleui.flashcards3.web.views.memory :as memory]
    [integrant.core :as ig]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]))

(defn route-data [opts]
  (merge
   opts
   {:muuntaja   formats/instance
    :middleware
    [auth/wrap-auth
     ;; query-params & form-params
     parameters/parameters-middleware
     ;; encoding response body
     muuntaja/format-response-middleware
     ;; exception handling
     exception/wrap-exception]}))

(derive :reitit.routes/ui :reitit/routes)

(defmethod ig/init-key :reitit.routes/ui
  [_ opts]
  [["/home" (route-data opts) (home/ui-routes opts)]
   ["/hours" (route-data opts) (hours/ui-routes opts)]
   ["/readings" (route-data opts) (readings/ui-routes opts)]
   ["/hours-total" (route-data opts) (hours-total/ui-routes opts)]
   ["/edit/:slideshow_id" (route-data opts) (edit/ui-routes opts)]
   ["/grid/:slideshow_id" (route-data opts) (grid/ui-routes opts)]
   ["/play/:slideshow_id/:step" (route-data opts) (play/ui-routes opts)]
   ["/play-double/:slideshow_id" (route-data opts) (play-double/ui-routes opts)]
   ["/play-effect/:slideshow_id" (route-data opts) (play-effect/ui-routes opts)]
   ["/jtd/:slideshow_id" (route-data opts) (jtd/ui-routes opts)]
   ["/play-write/:slideshow_id" (route-data opts) (play-write/ui-routes opts)]
   ["/play-exchange/:slideshow_id" (route-data opts) (play-exchange/ui-routes opts)]
   ["/play-guess/:slideshow_id" (route-data opts) (play-guess/ui-routes opts)]
   ["/word-search/:slideshow_id" (route-data opts) (word-search/ui-routes opts)]
   ["/memory/:slideshow_id" (route-data opts) (memory/ui-routes opts)]
   ["/fills/:reading_id" (route-data opts) (fill/ui-routes opts)]
   ["/scramble-paragraph/:reading_id" (route-data opts) (scramble-paragraph/ui-routes opts)]
   ["/scramble-sentence/:reading_id" (route-data opts) (scramble-sentence/ui-routes opts)]
   ["/studentss" (route-data opts) (students/ui-routes opts)]])
