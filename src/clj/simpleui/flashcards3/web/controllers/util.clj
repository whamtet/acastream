(ns simpleui.flashcards3.web.controllers.util)

(defn- compare-possible-nums [a b]
  (if (and a b (re-find #"^\d+$" a) (re-find #"^\d+$" b))
    (- (Long/parseLong a) (Long/parseLong b))
    (compare a b)))
(defn- compare-bits [[a & as] [b & bs]]
  (let [c (compare-possible-nums a b)]
    (if (and a (= 0 c))
      (compare-bits as bs)
      c)))
(defn compare-names [a b]
  (compare-bits
   (->> a .toLowerCase (re-seq #"\d+|\D+"))
   (->> b .toLowerCase (re-seq #"\d+|\D+"))))

(defn group-by-other [f s]
  (->> s
       (group-by f)
       (reduce
        (fn [m [k v]]
          (if (< (count v) 3)
            (update m "Other" concat v)
            (assoc m k v)))
        {"Other" ()})))
