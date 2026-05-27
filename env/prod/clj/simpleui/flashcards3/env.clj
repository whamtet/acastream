(ns simpleui.flashcards3.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[flashcards3 starting]=-"))
   :start      (fn []
                 (log/info "\n-=[flashcards3 started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[flashcards3 has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})

(def dev? false)
(def prod? true)

(defn host [& ss]
  (apply str "https://flashcards.simpleui.io" ss))
