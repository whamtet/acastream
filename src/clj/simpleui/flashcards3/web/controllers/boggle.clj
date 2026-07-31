(ns simpleui.flashcards3.web.controllers.boggle)

(def dice-str
  "AEANEG
WNGEEH
AHSPCO
LNHNRZ
ASPFFK
TSTIYD
OBJOAB
OWTOAT
IOTMUC
ERTTYL
RYVDEL
TOESSI
LREIXD
TERWHV
EIUNES
NUIHMQ")

(def dice (map #(.trim %) (.split dice-str "\n")))

(defn- rand-char [s]
  (let [c (rand-nth s)]
    (if (= \Q c)
      "Qu"
      (str c))))

(defn result []
  (->> dice
       (map rand-char)
       shuffle))
