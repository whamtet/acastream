(ns simpleui.flashcards3.web.routes.open
  (:require
    [simpleui.response :as response]
    [simpleui.flashcards3.web.middleware.exception :as exception]
    [simpleui.flashcards3.web.middleware.formats :as formats]
    [simpleui.flashcards3.web.views.battleships :as battleships]
    [simpleui.flashcards3.web.views.blooket :as blooket]
    [simpleui.flashcards3.web.views.dominos :as dominos]
    [simpleui.flashcards3.web.views.fill :as fill]
    [simpleui.flashcards3.web.views.icon-search :as icon-search]
    [simpleui.flashcards3.web.views.play-mobile :as play-mobile]
    [simpleui.flashcards3.web.views.students :as students]
    [simpleui.flashcards3.web.views.intro :as intro]
    [simpleui.flashcards3.web.views.snl :as snl]
    [simpleui.flashcards3.web.controllers.blooket :as controllers.blooket]
    [simpleui.flashcards3.web.controllers.email :as email]
    [simpleui.flashcards3.web.controllers.pdf-icons :as controllers.pdf-icons]
    [simpleui.flashcards3.web.controllers.pdf-battleships :as pdf-battleships]
    [simpleui.flashcards3.web.controllers.pdf-dice :as pdf-dice]
    [simpleui.flashcards3.web.controllers.pdf-snl :as pdf-snl]
    [simpleui.flashcards3.web.controllers.students :as controllers.students]
    [integrant.core :as ig]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]))

(defn route-data [opts]
  (merge
   opts
   {:muuntaja   formats/instance
    :middleware
    [;; query-params & form-params
     parameters/parameters-middleware
     ;; encoding response body
     muuntaja/format-response-middleware
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
   ["/mobile" (route-data opts) (play-mobile/ui-routes opts)]
   ["/pdf-icon"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "application/pdf"}
       :body (-> req :params :images pdf)})]
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
   ["/api/students" controllers.students/parse]
   ["/api/snl"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "application/pdf"}
       :body (pdf-snl/pdf req)})]
   ["/dominos" (route-data opts) (dominos/ui-routes opts)]
   ["/snl" (route-data opts) (snl/ui-routes opts)]
   ["/students" (route-data opts) (students/ui-routes opts)]])
