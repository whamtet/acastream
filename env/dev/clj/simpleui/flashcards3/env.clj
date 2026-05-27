(ns simpleui.flashcards3.env
  (:require
    [clojure.tools.logging :as log]
    [simpleui.flashcards3.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[flashcards3 starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[flashcards3 started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[flashcards3 has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev}})

(def dev? true)
(def prod? false)

(defn host [& ss]
  (apply str "http://localhost:3005" ss))
