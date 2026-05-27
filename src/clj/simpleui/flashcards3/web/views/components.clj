(ns simpleui.flashcards3.web.views.components)

(defn hiddens [& args]
  (for [[k v] (partition 2 args)
        v (if (coll? v) v [v])]
    [:input {:type "hidden"
             :id (when (= "id" v) k)
             :name k
             :value v}]))

(defn button
  ([label]
   [:span.p-1
    [:button {:type "button"
              :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white"}
     label]])
  ([label id]
   [:span.p-1
    [:button {:type "button"
              :id id
              :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white"}
     label]]))

(defn submit [label]
  [:span.p-1
   [:input {:type "submit"
            :value label
            :class "bg-clj-blue py-1.5 px-3 rounded-lg text-white"}]])

(defn submit-hidden [id]
  [:input {:id id :type "submit"}])

(defn button-warning [label] ;; see also warning below
  [:span.p-1
   [:button {:type "button"
             :class "bg-red-600 py-1.5 px-3 rounded-lg text-white"}
    label]])

(defn warning [msg]
  [:span {:class "bg-red-600 p-2 rounded-lg text-white"} msg])

(defn h1 [& contents]
  [:h1.text-3xl contents])

(defn h2 [& contents]
  [:h2.text-2xl contents])

(defn h3 [& contents]
  [:h3.text-xl contents])

(defn modal-scroll [width & contents]
  [:div#modal {:class "fixed left-0 top-0 w-full h-full
  z-10"
               :style {:background-color "rgba(0,0,0,0.4)"}
               :_ "on click if target.id === 'modal' add .hidden"}
   [:div {:class (str "mx-auto border rounded-lg bg-white overflow-y-auto overflow-x-clip " width)
          :style {:max-height "94vh"
                  :margin-top "3vh"
                  :margin-bottom "3vh"}}
    contents]])

(defn get-src [x]
  (if (string? x)
    (if (.startsWith x "http")
      (str "../../cache?src=" x)
      x)
    (format "../../local/%s" x)))

(defn get-src2 [x]
  (if (string? x)
    (if (.startsWith x "http")
      (str "../../../cache?src=" x)
      x)
    (format "../../../local/%s" x)))
