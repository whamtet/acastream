(ns simpleui.flashcards3.web.middleware.share
  (:require
    [simpleui.flashcards3.web.controllers.share :as share]))

(defn wrap-protect-share [handler]
  (fn [req]
    (if (share/open?)
      (handler req)
      {:status 403
       :headers {"Content-Type" "text/html"}
       :body "sorry my friend"})))
