(ns simpleui.flashcards3.web.controllers.boggle)

(def dice-str
  "AAAFRS AAEEEE AAFIRS ADENNN AEEEEM AEEGMU AEGMNN AFIRSY BJKQXZ CCNSTW CEIILT CEILPT CEIPST DHHNOT DHHLOR DHLNOR DDLNOR EIIITT EMOTTT ENSSSU FIPRSY GORRVW HIPRRY NOOTUW OOOTTU")

(def dice (re-seq #"\w+" dice-str))

(defn- rand-char [s]
  (let [c (rand-nth s)]
    (if (= \Q c)
      "Qu"
      (str c))))

(defn result []
  (->> dice
       (map rand-char)
       shuffle))
