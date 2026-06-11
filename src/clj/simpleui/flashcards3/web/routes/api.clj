(ns simpleui.flashcards3.web.routes.api
  (:require
    [simpleui.flashcards3.env :refer [dev?]]
    [simpleui.flashcards3.web.controllers.health :as health]
    [simpleui.flashcards3.web.controllers.pdf :as pdf]
    [simpleui.flashcards3.web.controllers.pdf-jtd :as pdf-jtd]
    [simpleui.flashcards3.web.controllers.pdf-bingo :as pdf-bingo]
    [simpleui.flashcards3.web.controllers.slideshow :as slideshow]
    [simpleui.flashcards3.web.controllers.students :as students]
    [simpleui.flashcards3.web.middleware.auth :as auth]
    [simpleui.flashcards3.web.middleware.exception :as exception]
    [simpleui.flashcards3.web.middleware.formats :as formats]
    [integrant.core :as ig]
    [reitit.coercion.malli :as malli]
    [reitit.ring.coercion :as coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.parameters :as parameters]
    [reitit.swagger :as swagger]))

(def route-data
  {:coercion   malli/coercion
   :muuntaja   formats/instance
   :swagger    {:id ::api}
   :middleware [auth/wrap-auth
                ;; query-params & form-params
                parameters/parameters-middleware
                ;; content-negotiation
                muuntaja/format-negotiate-middleware
                ;; encoding response body
                muuntaja/format-response-middleware
                ;; exception handling
                coercion/coerce-exceptions-middleware
                ;; decoding request body
                muuntaja/format-request-middleware
                ;; coercing response bodys
                coercion/coerce-response-middleware
                ;; coercing request parameters
                coercion/coerce-request-middleware
                ;; exception handling
                exception/wrap-exception]})

;; Routes
(defn api-routes [{:keys [query-fn]}]
  [["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title "simpleui.flashcards3 API"}}
           :handler (swagger/create-swagger-handler)}}]
   ["/pdf/:slideshow_id"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "application/pdf"}
       :body (->> req
                  :path-params
                  :slideshow_id
                  Long/parseLong
                  (pdf/get-pdf query-fn))})]
   ["/pdf-jtd"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "application/pdf"}
       :body (->> req
                  :params
                  pdf-jtd/pdf)})]
   ["/pdf-bingo/:slideshow_id"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "application/pdf"}
       :body (->> req
                  :path-params
                  :slideshow_id
                  Long/parseLong
                  (pdf-bingo/pdf
                   query-fn
                   (->> req
                        :params
                        :pages
                        Long/parseLong)
                   (->> req
                        :params
                        :scale
                        Double/parseDouble)))})]

   ["/studentss" students/parse]
   ["/qr/:slideshow_id"
    (fn [req]
      {:status 200
       :headers {"Content-Type" "image/png"}
       :body (->> req
                  :path-params
                  :slideshow_id
                  Long/parseLong
                  slideshow/mobile)})]
   (when dev?
     ["/session"
      (fn [req]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (pr-str (:session req))})])
   ["/health"
    ;; note that use of the var is necessary
    ;; for reitit to reload routes without
    ;; restarting the system
    {:get #'health/healthcheck!}]])

(derive :reitit.routes/api :reitit/routes)

(defmethod ig/init-key :reitit.routes/api
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  (fn [] [base-path route-data (api-routes opts)]))
