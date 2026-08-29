(ns simpleui.flashcards3.web.controllers.grammar
  (:require
    [clojure.string :as string]))

(defn- map2 [f1 f2 args]
  (loop [i 0
         [x & rest] args
         done ()]
    (cond
      (list? x) (recur i rest (conj done (f1 x)))
      (vector? x) (recur (inc i) rest (conj done (f2 i x)))
      :else (reverse done))))

(defn- rand-nth-leaf [s]
  (if (string? s) s (rand-nth s)))
(defn- underscore-leaf [s]
  (let [i (if (string? s) (count s) (->> s (map count) (apply max)))]
    (-> i (+ 2) (repeat "_") string/join)))

(defn- format-gen* [s args]
  (let [matches (filter vector? args)
        m (count matches)
        n (-> matches first count)]
    (assert (every? #(-> % count (= n)) matches))
    (fn [i]
      (let [match-select (rand-int n)
            underscore-index (mod (- m i 1) m)]
        (apply format s
               (map2
                rand-nth
                (fn [i x]
                  (-> match-select x ((if (= i underscore-index) underscore-leaf rand-nth-leaf))))
                args))))))

(defn- transpose [m]
  (for [i (-> m first count range)]
    (mapv #(% i) m)))
(defn- split-pipe [^String s]
  (vec (.split s " \\| ")))

(defn- format-gen [[s & args]]
  (format-gen*
   s
   (if (every? string? args)
     (->> args (map split-pipe) transpose)
     args)))

(defn- random-format [[s & args]]
  (->> args (map rand-nth) (apply format s)))

(def data
  '{"Look 2.1"
    ("%s have %s on %s? %s."
      ["Do you" "Does he" "Does she" "Do they"]
      ("art" "computers" "English" "gym" "math" "music" "reading" "science")
      ("Monday" "Tuesday" "Wednesday" "Thursday" "Friday")
      [["Yes, I do" "No, I don't"]
       ["Yes, he does" "No, he doesn't"]
       ["Yes, she does" "No, she doesn't"]
       ["Yes, they do" "No, they don't"]])
    "Look 2.3"
    ("%s %s. %s %s."
      ["This is our" "That's their"]
      ("living room" "dining room" "kitchen")
      ["We have" "They have"]
      ("an armchair" "a bookcase" "a door" "a floor" "a mirror" "a rug" "a window"))
    "Look 2.4.2"
    ("%s %s? %s."
      ["Do you" "Do you" "Do the players"]
      ("watch basketball" "ride unicycles" "play baseball" "play basketball" "play hockey" "play tennis")
      [["Yes, I do" "No, I don't"]
       ["Yes, we do" "No, we don't"]
       ["Yes, they do" "No, they don't"]])
    "Look 2.5.2"
    ("%s %s? %s."
      ["Does Paul" "Does he" "Does she" "Does a teacher" "Does a doctor" "Does a taxi driver"]
      ("use a lot of building bricks" "build what's on the box" "use a board" "score goals" "drive people home")
      [["Yes, he does" "No, he doesn't"]
       ["Yes, he does" "No, he doesn't"]
       ["Yes, she does" "No, she doesn't"]
       ["Yes, she does" "No, she doesn't"]
       ["Yes, he does" "No, he doesn't"]
       ["Yes, he does" "No, he doesn't"]])
    "Look 2.7.1"
    ("%s %s."
      "There are some | beans"
      "There aren't any | beans"
      "There is some | cheese"
      "There isn't any | cheese"
      "There is some | chicken"
      "There isn't any | chicken"
      "There are some | eggs"
      "There aren't any | eggs"
      "There are some | fries"
      "There aren't any | fries"
      "There are some | grapes"
      "There aren't any | grapes"
      "There is some | juice"
      "There isn't any | juice"
      "There are some | mangoes"
      "There aren't any | mangoes"
      "There are some | pears"
      "There aren't any | pears"
      "There are some | sausages"
      "There aren't any | sausages")
    })

(def funcs
  (update-vals data format-gen))

(doseq [line (->> 10 range (map (funcs "Look 2.7.1")))]
  (println line))
