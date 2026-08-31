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
    (assert (not-empty matches))
    (assert (every? #(-> % count (= n)) matches) (format "Error for %s: %s %s" n (pr-str matches) (pr-str (map count matches))))
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
(defn- merge-pairs [[s v]]
  (conj (split-pipe s) v))

(defn- format-gen [[s & args]]
  (format-gen*
   s
   (cond
     (every? string? args)
     (->> args (map split-pipe) transpose)
     (some string? args)
     (->> args (partition 2) (map merge-pairs) transpose)
     :else
     args)))

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
    "Look 2.7.2"
    ("%s %s? %s."
      "Are there any | beans" ["Yes, there are" "No, there aren't"]
      "Is there any | cheese" ["Yes, there is" "No, there isn't"]
      "Is there any | chicken" ["Yes, there is" "No, there isn't"]
      "Are there any | eggs" ["Yes, there are" "No, there aren't"]
      "Are there any | fries" ["Yes, there are" "No, there aren't"]
      "Are there any | grapes" ["Yes, there are" "No, there aren't"]
      "Is there any | juice" ["Yes, there is" "No, there isn't"]
      "Are there any | mangoes" ["Yes, there are" "No, there aren't"]
      "Are there any | pears" ["Yes, there are" "No, there aren't"]
      "Are there any | sausages" ["Yes, there are" "No, there aren't"])
    "Look 2.8.2"
    ("What %s doing? %s %s"
      ["are you" "is he" "is she" "are they"]
      ["I'm" "He's" "She's" "They're"]
      ("dancing" "drinking" "eating" "holding a box" "listening to music"
                 "taking photos" "holding a balloon" "drinking lemonade"))
    "Look 2.9.1"
    ("%s the %s %s? %s."
      ["Is" "Are"]
      [["crocodile" "elephant" "giraffe" "hippo" "lion" "monkey" "snake" "tiger" "zebra"]
       ["crocodiles" "elephants" "giraffes" "hippos" "lions" "monkeys" "snakes" "tigers" "zebras"]]
      ("sleeping" "running" "crying" "drinking" "pooping" "eating")
      [["Yes, it is" "No, it isn't"] ["Yes, they are" "No, they aren't"]])
    "Look 2.11.1"
    ("%s %s? %s %s."
      ["How do you" "When does the" "Where does the" "Where does the"]
      ["get to school" "bus come" "bus go" "plane go"]
      ["I go by" "It comes at" "It goes to" "It goes to"]
      [["bus" "car" "motorcycle" "bike"]
       ["7:00" "8:00" "9:00" "10:00"]
       ["the library" "school" "the park" "the market"]
       ["Hanoi" "Da Nang" "Nha Trang" "America"]])
    "Look 2.11.2"
    ("%s %s"
      ["baby" "beach" "box" "bus" "dish" "scarf" "tomato" "child" "fish" "foot" "man" "mouse" "person" "sheep"]
      ["babies" "beaches" "boxes" "buses" "dishes" "scarves" "tomatoes" "children" "fish" "feet" "men" "mice" "people" "sheep"])
    "Look 3.1.1"
    ("%s %s? %s %s."
      ["Are you" "Where are" "Where is" "Where is" "Where are"]
      [["from Japan" "from Vietnam" "from Thailand" "from America"]
       "you from"
       "he from"
       "she from"
       "they from"]
      [["Yes, I am" "No, I'm not"]
       "I'm from"
       "He's from"
       "She's from"
       "They're from"]
      [""
       ["Japan" "Vietnam" "Thailand" "America"]
       ["Japan" "Vietnam" "Thailand" "America"]
       ["Japan" "Vietnam" "Thailand" "America"]
       ["Japan" "Vietnam" "Thailand" "America"]])
    "Look 3.2.1"
    ("%s live %s? %s."
      ["Does Carlos" "Does Lily" "Do Rita and Lucas" "Do you" "Does your friend"]
      ("in a village" "in a town" "near a river" "next to a lake" "near school")
      [["Yes, he does" "No, he doesn't"]
       ["Yes, she does" "No, she doesn't"]
       ["Yes, they do" "No, they don't"]
       ["Yes, I do" "No, I don't"]
       ["Yes, he does" "No, she doesn't"]])
    "Look 3.3.2"
    ("How often %s %s? %s."
      ["do you" "does your friend"]
      [["clean up your room" "help your mom" "ride your bike" "watch a movie" "wash your hair"]
       ["clean up their room" "help their mom" "ride their bike" "watch a movie" "wash their hair"]]
      [["Never" "Sometimes" "Usually" "Always" "Twice a week" "Once a month"]
       ["Never" "Sometimes" "Usually" "Always" "Twice a week" "Once a month"]])
    "Look 3.4.2"
    ("%s like %s? %s."
      ["Does Ed" "Does Sara" "Does Pedro" "Does Tina" "Does Jun" "Do Fatima and Neema"]
      ("riding horses" "playing soccer" "dressing up")
      [["Yes, he does" "No, he doesn't"]
       ["Yes, she does" "No, she doesn't"]
       ["Yes, he does" "No, he doesn't"]
       ["Yes, she does" "No, she doesn't"]
       ["Yes, he does" "No, he doesn't"]
       ["Yes, they do" "No, they don't"]])
    "Look 3.5.1"
    ("%s %s %s"
      ["There is" "There are" "There aren't" "There isn't" "Is there" "Are there"]
      ["some" "a lot of" "any" "any" "any" "any"]
      [["ketchup." "pasta." "salad." "soup."]
       ["milkshakes." "noodles." "pancakes." "sandwiches." "vegetables."]
       ["milkshakes." "noodles." "pancakes." "sandwiches." "vegetables."]
       ["ketchup." "pasta." "salad." "soup."]
       ["ketchup?" "pasta?" "salad?" "soup?"]
       ["milkshakes?" "noodles?" "pancakes?" "sandwiches?" "vegetables?"]])
    "Look 3.5.2"
    ("Can I have a %s of %s, please?"
      ["glass" "bottle" "bowl" "plate" "slice" "bag"]
      [["water" "juice"] ;; glass
       ["juice" "water" "ketchup"] ;; bottle
       ["rice" "pasta" "fries" "ice cream" "noodles"] ;; bowl
       ["pasta" "rice" "fries" "noodles" "potatoes"] ;; plate
       ["bread" "apple" "cake" "meat" "pizza"] ;; slice
       ["grapes" "apples" "candy"] ;; bag
       ])
    "Look 3.9.1"
    ("%s there %s?  %s."
      ["Was" "Were"]
      [["a building" "a movie theater" "a bus stop" "a parking lot" "a cafe" "a sports center" "a hospital" "a supermarket" "a market"]
       ["any buildings" "any movie theaters" "any bus stops" "any parking lots" "any cafes"
        "any sports centers" "any hospitals" "any supermarkets" "any markets"]]
      [["Yes, there was" "No, there wasn't"]
       ["Yes, there were" "No, there weren't"]])
    "Look 3.10.1"
    ("The ancient Maya people %s %s."
      ["lived" "didn't live" "danced" "painted" "didn't paint" "traveled" "didn't travel" "liked" "didn't like"]
      ["in Mexico"
       "in China"
       "for fun"
       "their bodies"
       "portraits"
       "by foot"
       "by car"
       "chocolate"
       "doing their homework"])
    "Look 3.11.1"
    ("On the vacation they %s %s."
      [["ate" "didn't eat"]
       ["got" "didn't get"]
       ["went" "didn't go"]
       ["went" "didn't go"]
       ["had" "didn't have"]
       ["made" "didn't make"]
       ["rode" "didn't ride"]
       ["saw" "didn't see"]
       ["slept" "didn't sleep"]
       ["swam" "didn't swim"]]
      ["outside"
       "lost"
       "canoeing"
       "on a roller coaster"
       "a picnic"
       "friends"
       "on a motorcycle"
       "a shooting star"
       "in a tent"
       "in a lake"])
    "Look 3.12.1"
    ("We are %s %s."
      ["going to see"
       "going to learn about"
       "going to play at"
       "going to visit"
       "going to ride on"
       "going to touch"
       "going to play at"
       "going on rides at"
       "going to swim at"
       "going to see the animals at"]
      ["the art gallery"
       "dinosaurs"
       "the fair"
       "the museum"
       "the ride"
       "the sculpture"
       "summer camp"
       "the theme park"
       "the water park"
       "the wildlife park"])
    "Look 3.12.2"
    ("Is %s going %s?  %s."
      ["Joe" "Padma" "Sunil" "Kristiina" "Jimena"]
      ("to summer camp" "to the theme park" "camping" "to the wildlife park" "to the fair" "to the museum"
                        "to the art gallery")
      [["Yes, he is" "No, he isn't"]
       ["Yes, she is" "No, she isn't"]
       ["Yes, he is" "No, he isn't"]
       ["Yes, she is" "No, she isn't"]
       ["Yes, she is" "No, she isn't"]])
    })

(def funcs
  (update-vals data format-gen))
