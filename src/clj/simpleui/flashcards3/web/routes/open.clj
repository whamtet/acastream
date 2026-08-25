(ns simpleui.flashcards3.web.routes.open
  (:require
    [simpleui.response :as response]
    [simpleui.flashcards3.web.middleware.exception :as exception]
    [simpleui.flashcards3.web.middleware.share :as middleware.share]
    [simpleui.flashcards3.web.views.battleships :as battleships]
    [simpleui.flashcards3.web.views.blooket :as blooket]
    [simpleui.flashcards3.web.views.boggle :as boggle]
    [simpleui.flashcards3.web.views.dominos :as dominos]
    [simpleui.flashcards3.web.views.fill :as fill]
    [simpleui.flashcards3.web.views.icon-search :as icon-search]
    [simpleui.flashcards3.web.views.students :as students]
    [simpleui.flashcards3.web.views.intro :as intro]
    [simpleui.flashcards3.web.views.share :as share]
    [simpleui.flashcards3.web.views.snl :as snl]
    [simpleui.flashcards3.web.views.vocab :as vocab]
    [simpleui.flashcards3.web.controllers.cache :as cache]
    [simpleui.flashcards3.web.controllers.local :as local]
    [simpleui.flashcards3.web.controllers.blooket :as controllers.blooket]
    [simpleui.flashcards3.web.controllers.email :as email]
    [simpleui.flashcards3.web.controllers.pdf-icons :as controllers.pdf-icons]
    [simpleui.flashcards3.web.controllers.pdf-battleships :as pdf-battleships]
    [simpleui.flashcards3.web.controllers.pdf-dice :as pdf-dice]
    [simpleui.flashcards3.web.controllers.pdf-snl :as pdf-snl]
    [simpleui.flashcards3.web.controllers.pdf-trace :as pdf-trace]
    [simpleui.flashcards3.web.controllers.schedule :as schedule]
    [simpleui.flashcards3.web.controllers.students :as controllers.students]
    [integrant.core :as ig]
    [reitit.ring.middleware.parameters :as parameters]))

(defn route-data [opts]
  (merge
   opts
   {:middleware
    [;; query-params & form-params
     parameters/parameters-middleware
     ;; exception handling
     exception/wrap-exception]}))

(defn route-data-share [opts]
  (merge
   opts
   {:middleware
    [;; query-params & form-params
      parameters/parameters-middleware
      middleware.share/wrap-protect-share
      ;; exception handling
      exception/wrap-exception]}))

(derive :reitit.routes/open :reitit/routes)

(defn- pdf [images]
  (controllers.pdf-icons/svg->pdf
   (if (string? images)
     [images]
     images)))

(defmethod ig/init-key :reitit.routes/open
  [_ opts]
  [["" (route-data opts) (intro/ui-routes opts)]
   ["/fill" (route-data opts) (fill/ui-routes opts)]
   ["/icon-search" (route-data opts) (icon-search/ui-routes opts)]
   ["/email"
    (fn [req]
      (email/send-params (:params req))
      (response/redirect "https://acastream.uk/reply.html"))]
   ["/cache"
    (fn [req]
      (-> req
          :params
          :src
          cache/cache))]
   ["/local/:local_id"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "image/jpeg"}
       :body (->> req
                  :path-params
                  :local_id
                  Long/parseLong
                  local/input-stream)})]
   ["/pdf-icon"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "application/pdf"}
       :body (-> req :params :images pdf)})]
   ["/pdf-trace"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "application/pdf"}
       :body (-> req :params pdf-trace/get-pdf)})]
   ["/pdf-battleships"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "application/pdf"}
       :body (-> req :params pdf-battleships/pdf)})]
   ["/pdf-dice"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "application/pdf"}
       :body (-> req :params pdf-dice/pdf)})]
   ["/battleships" (route-data opts) (battleships/ui-routes opts)]
   ["/battleships-demo" battleships/demo]
   ["/blooket" (route-data opts) (blooket/ui-routes opts)]
   ["/blooket-csv"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "text/csv"}
       :body (-> req :params controllers.blooket/csv)})]
   ["/kahoot"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}
       :body (-> req :params controllers.blooket/kahoot)})]
   ["/cal"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "text/calendar; charset=utf-8"
                 "Content-Disposition" "inline; filename=\"calendar.ics\""
                 "Cache-Control" "no-cache, no-store, must-revalidate"
                 "Access-Control-Allow-Origin" "*"}
       :body (schedule/cal-body)})]
   ["/api/students" controllers.students/parse]
   ["/api/snl"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "application/pdf"}
       :body (pdf-snl/pdf req)})]
   ["/dominos" (route-data opts) (dominos/ui-routes opts)]
   ["/snl" (route-data opts) (snl/ui-routes opts)]
   ["/s" (share/ui-routes-qr opts)]
   ["/boggle" (boggle/boggle-handler opts)]
   ["/share" (route-data-share opts) (share/ui-routes opts)]
   ["/students" (route-data opts) (students/ui-routes opts)]
   ["/vocabs" (route-data opts) (vocab/ui-routes-vocabs opts)]
   ["/vocab/:slideshow_id" (route-data opts) (vocab/ui-routes-vocab opts)]])
