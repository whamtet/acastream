(ns simpleui.flashcards3.web.middleware.auth
  (:require
    [ring.middleware.basic-authentication
     :refer [wrap-basic-authentication]]
    [simpleui.flashcards3.env :refer [prod?]]))

(defn authenticated? [name pass]
  (and (= name (System/getenv "SLIDESHOW_USER"))
       (= pass (System/getenv "SLIDESHOW_PASS"))))

(defn wrap-auth [handler]
  (cond-> handler
    prod? (wrap-basic-authentication authenticated?)))
