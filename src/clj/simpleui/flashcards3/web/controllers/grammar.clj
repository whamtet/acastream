(ns simpleui.flashcards3.web.controllers.grammar
  (:require
    [clojure.string :as string]
    [simpleui.flashcards3.web.controllers.grammar.underscore :as underscore]
    [simpleui.flashcards3.web.controllers.util :as util]))

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
  (if (empty? s)
    ""
    (let [i (if (string? s) (count s) (->> s (map count) (apply max)))]
      (-> i (+ 2) (repeat "_") string/join))))

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
  (case s
    :underscore (underscore/underscore-gen args)
    :multiple
    (let [fs (map format-gen args)]
      (assert (not-empty fs))
      #((rand-nth fs) %))
    (format-gen*
     s
     (cond
       (every? string? args)
       (->> args (map split-pipe) transpose)
       (some string? args)
       (->> args (partition 2) (map merge-pairs) transpose)
       :else
       args))))

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
    "Look 2.4.4"
    ("%s %s? %s."
      ["Do you" "Do you" "Do the players"]
      ("watch basketball" "ride unicycles" "play baseball" "play basketball" "play hockey" "play tennis")
      [["Yes, I do" "No, I don't"]
       ["Yes, we do" "No, we don't"]
       ["Yes, they do" "No, they don't"]])
    "Look 2.5.4"
    ("%s %s? %s."
      ["Does Paul" "Does he" "Does she" "Does a teacher" "Does a doctor" "Does a taxi driver"]
      ("use a lot of building bricks" "build what's on the box" "use a board" "score goals" "drive people home")
      [["Yes, he does" "No, he doesn't"]
       ["Yes, he does" "No, he doesn't"]
       ["Yes, she does" "No, she doesn't"]
       ["Yes, she does" "No, she doesn't"]
       ["Yes, he does" "No, he doesn't"]
       ["Yes, he does" "No, he doesn't"]])
    "Look 2.7.2"
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
    "Look 2.7.4"
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
    "Look 2.8.4"
    ("What %s doing? %s %s"
      ["are you" "is he" "is she" "are they"]
      ["I'm" "He's" "She's" "They're"]
      ("dancing" "drinking" "eating" "holding a box" "listening to music"
                 "taking photos" "holding a balloon" "drinking lemonade"))
    "Look 2.9.2"
    ("%s the %s %s? %s."
      ["Is" "Are"]
      [["crocodile" "elephant" "giraffe" "hippo" "lion" "monkey" "snake" "tiger" "zebra"]
       ["crocodiles" "elephants" "giraffes" "hippos" "lions" "monkeys" "snakes" "tigers" "zebras"]]
      ("sleeping" "running" "crying" "drinking" "pooping" "eating")
      [["Yes, it is" "No, it isn't"] ["Yes, they are" "No, they aren't"]])
    "Look 2.11.2"
    ("%s %s? %s %s."
      ["How do you" "When does the" "Where does the" "Where does the"]
      ["get to school" "bus come" "bus go" "plane go"]
      ["I go by" "It comes at" "It goes to" "It goes to"]
      [["bus" "car" "motorcycle" "bike"]
       ["7:00" "8:00" "9:00" "10:00"]
       ["the library" "school" "the park" "the market"]
       ["Hanoi" "Da Nang" "Nha Trang" "America"]])
    "Look 2.11.4"
    ("%s %s"
      ["baby" "beach" "box" "bus" "dish" "scarf" "tomato" "child" "fish" "foot" "man" "mouse" "person" "sheep"]
      ["babies" "beaches" "boxes" "buses" "dishes" "scarves" "tomatoes" "children" "fish" "feet" "men" "mice" "people" "sheep"])
    "Look 3.1.2"
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
    "Look 3.2.2"
    ("%s live %s? %s."
      ["Does Carlos" "Does Lily" "Do Rita and Lucas" "Do you" "Does your friend"]
      ("in a village" "in a town" "near a river" "next to a lake" "near school")
      [["Yes, he does" "No, he doesn't"]
       ["Yes, she does" "No, she doesn't"]
       ["Yes, they do" "No, they don't"]
       ["Yes, I do" "No, I don't"]
       ["Yes, he does" "No, she doesn't"]])
    "Look 3.3.4"
    ("How often %s %s? %s."
      ["do you" "does your friend"]
      [["clean up your room" "help your mom" "ride your bike" "watch a movie" "wash your hair"]
       ["clean up their room" "help their mom" "ride their bike" "watch a movie" "wash their hair"]]
      [["Never" "Sometimes" "Usually" "Always" "Twice a week" "Once a month"]
       ["Never" "Sometimes" "Usually" "Always" "Twice a week" "Once a month"]])
    "Look 3.4.4"
    ("%s like %s? %s."
      ["Does Ed" "Does Sara" "Does Pedro" "Does Tina" "Does Jun" "Do Fatima and Neema"]
      ("riding horses" "playing soccer" "dressing up")
      [["Yes, he does" "No, he doesn't"]
       ["Yes, she does" "No, she doesn't"]
       ["Yes, he does" "No, he doesn't"]
       ["Yes, she does" "No, she doesn't"]
       ["Yes, he does" "No, he doesn't"]
       ["Yes, they do" "No, they don't"]])
    "Look 3.5.2"
    ("%s %s %s"
      ["There is" "There are" "There aren't" "There isn't" "Is there" "Are there"]
      ["some" "a lot of" "any" "any" "any" "any"]
      [["ketchup." "pasta." "salad." "soup."]
       ["milkshakes." "noodles." "pancakes." "sandwiches." "vegetables."]
       ["milkshakes." "noodles." "pancakes." "sandwiches." "vegetables."]
       ["ketchup." "pasta." "salad." "soup."]
       ["ketchup?" "pasta?" "salad?" "soup?"]
       ["milkshakes?" "noodles?" "pancakes?" "sandwiches?" "vegetables?"]])
    "Look 3.5.4"
    ("Can I have a %s of %s, please?"
      ["glass" "bottle" "bowl" "plate" "slice" "bag"]
      [["water" "juice"] ;; glass
       ["juice" "water" "ketchup"] ;; bottle
       ["rice" "pasta" "fries" "ice cream" "noodles"] ;; bowl
       ["pasta" "rice" "fries" "noodles" "potatoes"] ;; plate
       ["bread" "apple" "cake" "meat" "pizza"] ;; slice
       ["grapes" "apples" "candy"] ;; bag
       ])
    "Look 3.9.4"
    ("%s there %s?  %s."
      ["Was" "Were"]
      [["a building" "a movie theater" "a bus stop" "a parking lot" "a cafe" "a sports center" "a hospital" "a supermarket" "a market"]
       ["any buildings" "any movie theaters" "any bus stops" "any parking lots" "any cafes"
        "any sports centers" "any hospitals" "any supermarkets" "any markets"]]
      [["Yes, there was" "No, there wasn't"]
       ["Yes, there were" "No, there weren't"]])
    "Look 3.10.2"
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
    "Look 3.11.2"
    ("On the vacation they %s %s."
      [["ate" "didn't eat"]
       ["got" "didn't get"]
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
       "a picnic"
       "friends"
       "on a motorcycle"
       "a shooting star"
       "in a tent"
       "in a lake"])
    "Look 3.12.2"
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
    "Look 3.12.4"
    ("Is %s going %s?  %s."
      ["Joe" "Padma" "Sunil" "Kristiina" "Jimena"]
      ("to summer camp" "to the theme park" "camping" "to the wildlife park" "to the fair" "to the museum"
                        "to the art gallery")
      [["Yes, he is" "No, he isn't"]
       ["Yes, she is" "No, she isn't"]
       ["Yes, he is" "No, he isn't"]
       ["Yes, she is" "No, she isn't"]
       ["Yes, she is" "No, she isn't"]])
    "Look 4.1.4"
    ("%s %s."
      ["An actor" "A clown" "A dentist" "A firefighter" "A nurse" "A photographer" "A pilot" "A police officer" "A server"]
      ["acts" "makes children laugh" "fixes teeth" "fights fires"
       "cares for patients" "takes photos" "flies planes"
       "catches criminals" "serves food and drinks"])
    "Look 4.2.4"
    ("They %s %s."
      ["played the cello" "played classical music" "performed a concert"
       "danced" "played the drums" "played the flute" "played the keyboard"
       "played pop music" "sang" "played the violin"]
      [["loudly" "well" "slowly" "quickly" "quietly" "carefully"]
       ["loudly" "well" "slowly" "quickly" "quietly" "carefully"]
       ["loudly" "well" "slowly" "quickly" "quietly" "carefully"]
       ["well" "slowly" "quickly"]
       ["loudly" "well" "slowly" "quickly" "quietly" "carefully"]
       ["loudly" "well" "slowly" "quickly" "quietly" "carefully"]
       ["loudly" "well" "slowly" "quickly" "quietly" "carefully"]
       ["loudly" "well" "slowly" "quickly" "quietly" "carefully"]
       ["loudly" "well" "slowly" "quickly" "quietly" "carefully"]
       ["loudly" "well" "slowly" "quickly" "quietly" "carefully"]])
    "Look 4.3.2"
    ("We %s %s."
      ["ate" "gave" "made" "played" "put up" "threw" "watched" "wore"]
      ["traditional food"
       "presents"
       "special food"
       "party games"
       "decorations"
       "streamers"
       "a parade"
       "traditional clothes"])
    "Look 4.3.4"
    (:underscore
      "_Where did_ Mica _go_?  She _went_ to Florence."
      "_What did_ he _buy_?  He _bought_ a ticket."
      "_When did_ Anna _go to_ Perugia?  She _went_ in October."
      "_What did_ people _make_ from chocolate pieces?  They _made_ sculptures."
      "_What did_ Anna _eat_?  She _ate_ chocolate.")
    "Look 4.4.2"
    (:underscore
      "There are many sports they _can_ play."
      "They _can_ win medals, too."
      "Soon she _could not_ see at all."
      "She _could not_ play basketball anymore."
      "There was a sport she _could_ play: goalball!")
    "Look 4.5.2"
    (:underscore
      "Divers discovered the _longest_ underwater cave."
      "The _oldest_ paintings in the world are in a cave in northern Spain."
      "The Atacama Desert is the _driest_ desert in the world."
      "The _hottest_ ocean in the world is the Indian Ocean."
      "Many people think the _best_ waves for surfing are in Australia.")
    "Look 4.5.4"
    (:underscore
      "Burj Khalifa became the _tallest_ building in the world."
      "It was one of the _most expensive_ projects ever."
      "It's _more famous_ than other buildings in Dubai."
      "It has the _highest_ restaurant in the world too."
      "The view from the top is _more exciting_ at night than during the day."
      "The animals in Nepal are _more interesting_ than the animals in Costa Rica."
      "Some of the _most famous_ mountains in the world are in Nepal."
      "The mountains in Costa Rica are _less difficult_ to climb than the mountains in Nepal."
      "The beach at Tamarindo is one of the _most popular_ in Costa Rica."
      "Surfing is _more exciting_ at Tamarindo than at other beaches."
      "The snowboarding lesson is _more exciting_ than the boat trip."
      "The museum visit is _more boring_ than the snowboarding lesson."
      "The boat trip is _more interesting_ than the museum visit."
      "I think the snowboarding lesson is the _best_ activity."
      "I think the _museum visit_ is the _most boring_ activity.")
    "Look 4.5.2, 4.5.4"
    (:underscore
      "Divers discovered the _longest_ underwater cave."
      "The _oldest_ paintings in the world are in a cave in northern Spain."
      "The Atacama Desert is the _driest_ desert in the world."
      "The _hottest_ ocean in the world is the Indian Ocean."
      "Many people think the _best_ waves for surfing are in Australia."
      "Burj Khalifa became the _tallest_ building in the world."
      "It was one of the _most expensive_ projects ever."
      "It's _more famous_ than other buildings in Dubai."
      "It has the _highest_ restaurant in the world too."
      "The view from the top is _more exciting_ at night than during the day.")
    "Look 4.6.2"
    (:underscore
      "You can't use that pen.  It's not _yours_."
      "_Whose_ coat is this?  I really like it."
      "Are these your sunglasses?  Yes, they're _mine_."
      "Where are Jack and Finn?  These drinks are _theirs_."
      "No, those sneakers aren't _hers_."
      "Are those sneakers David's?  Yes, I think they're _his_."
      "Whose hat is that with stripes?  It's _mine_."
      "She says they're not _hers_."
      "Are they yours?  No, they aren't _mine_."
      "Our family is big.  That big car is _ours_."
      "Is that their house?  Yes, it is _theirs_.")
    "Look 4.6.4"
    (:underscore
      "We _wear_ hats _to keep_ our heads warm."
      "We _wear_ sunglasses _to protect_ our eyes."
      "We _use_ umbrellas _to protect_ us from the rain."
      "We _wear_ sports T-shirts _to show_ that we like a team."
      "We _wear_ gloves to keep our hands warm."
      "I walked to the bus stop _to catch_ a bus."
      "I went to the shopping mall _to buy_ some new sneakers."
      "I needed the sneakers _to play_ volleyball."
      "I'm on a volleyball team _to make_ new friends."
      "I practice volleyball every week _to learn_ how to play better."
      "People go to cafes _to drink_ coffee."
      "People play sports _to have_ fun."
      "I go to VUS _to learn_ English and _to have_ fun.")
    "Look 4.7.2"
    (:underscore
      "People didn't watch TV or _listen to_ the radio."
      "How did people _travel to_ places far away?"
      "They couldn't _look at_ a clock."
      "Did people _wait for_ the sun to come up?"
      "How did people clean their teeth before they _went to_ bed?"
      "Did people _think about_ work all the time?"
      "How did people _go between_ different floors in a building?"
      "How did people _look at_ their faces without mirrors?"
      "How did people _travel to_ other countries before there were planes?"
      "How did people _listen to_ music before there were radios?"
      "How could people _look for_ answers to questions without the internet?"
      "What type of music do you like _listening to_?"
      "What do you _talk about_ with your friends?"
      "Where did you _travel to_ on your last vacation?"
      "What things do you like _looking for_ in museums?")
    "Look 4.7.4"
    (:underscore
      "You _have to_ play it in the gym."
      "Children _have to_ stand in a line."
      "They _have to_ bend down."
      "One child _has to_ jump over all the children."
      "He or she _has to_ keep his or her feet off the ground."
      "The other children _have to_ stand still.  It's not that easy!"
      "If someone _has to take_ a bath, they turn on the faucet."
      "150 years ago, people _had to put_ hot water on a fire."
      "150 years ago, people _had to wear_ a lot of clothes in the house."
      "People _had to sit_ near the fire to stay warm.")
    "Look 4.8.2"
    (:underscore
      "_How many_ broccoli are there?"
      "_How much_ cereal do you have for breakfast?"
      "_How many_ chili peppers can you eat?"
      "_How much_ corn can you eat?"
      "_How much_ zucchini can you eat?"
      "_How much_ jam do you like on your bread?"
      "_How much_ lettuce in a hamburger?"
      "_How many_ nuts in a jar?"
      "_How many_ olives in a salad?"
      "_How many_ strawberries on the cake?")
    "Look 4.8.4"
    ("%s %s %s"
      ["Are there"
       "Is there"
       "There"
       "There"
       "There"
       "There"
       "They ate"
       "They ate"
       "They grew"]
      ["any"
       "any"
       "are some"
       "is some"
       "aren't any"
       "isn't any"
       "a few"
       "a little"
       "a lot of"]
      [["broccoli?" "chili peppers?" "nuts?" "olives?" "strawberries?"]
       ["cereal?" "corn?" "zucchini?" "jam?" "lettuce?"]
       ["broccoli." "chili peppers." "nuts." "olives." "strawberries."]
       ["cereal." "corn." "zucchini." "jam." "lettuce."]
       ["broccoli." "chili peppers." "nuts." "olives." "strawberries."]
       ["cereal." "corn." "zucchini." "jam." "lettuce."]
       ["broccoli." "chili peppers." "nuts." "olives." "strawberries."]
       ["cereal." "corn." "zucchini." "lettuce."]
       ["broccoli." "chili peppers." "nuts." "olives." "strawberries." "jam."
        "cereal." "corn." "zucchini." "lettuce."]])
    "Look 4.9.2"
    ("%s %s %s."
      [["The robot" "The e-book" "The headphones" "The interactive whiteboard"
        "The laptop" "The microphone" "The VR headset" "The Wi-Fi"]
       ["The robot" "The e-book" "The headphones" "The interactive whiteboard"
        "The laptop" "The microphone" "The VR headset" "The Wi-Fi"]
       "One day,"
       "In 2040,"
       "Fifteen years from now"
       ]
      ["will help"
       "won't help"
       "we'll have"
       "we won't have"
       "we'll use"]
      ["a lot of students"
       "any students"
       ["robots in our class" "VR headsets" "interactive whiteboards"]
       ["pens" "pencils" "erasers"]
       ["AI" "robot teachers" "robots dogs"]])
    "Look 4.9.4"
    (:multiple
      ("%s there %s more %s in the future?  %s."
        ["Will"]
        ["be"]
        ("drones" "apps" "e-books" "interactive whiteboards" "laptops" "VR headsets")
        [["Yes, there will" "No, there won't"]])
      ("In the future %s %s %s."
        ["people" "robots" "people" "robots" "robots"]
        ["won't go" "will work" "won't have" "will work" "will be"]
        ["to the supermarket" "in the house" "to drive" "as doctors" "police officers"])
      (:underscore
        "Children _won't wear_ school uniforms."
        "Children _will go_ to school in flying cars."
        "Children _will have_ robot teachers."
        "Children _won't write_ with pencils and pens."
        "Children _won't play_ traditional games outside."
        "Children _will talk_ to friends around the world with VR headsets."))
    "Look 4.10.2"
    (:multiple
      ("%s %s."
        ["You should"
         "Your bedroom should"
         "You shouldn't"
         "Your bedroom shouldn't"]
        [["fall asleep" "rest" "get exercise" "be strong"]
         ["be dark at night" "be dry"]
         ["awake too early" "be weak"]
         ["be wet" "be light at night"]])
      (:underscore
        "You _should drink_ a lot of water and hot tea."
        "You _should wear_ warm clothes."
        "You _shouldn't go_ to a party."
        "You _should rest_ and watch movies on the sofa."
        "You _shouldn't go_ near babies or old people."
        "You _should stay_ home until you are better."))
    "Look 4.11.2"
    ("%s %s the %s."
      "He's | been to" ["airport" "bus station" "fire station" "hotel" "pharmacy" "police station" "restaurant" "square" "train station" "university"]
      "She's | been to" ["airport" "bus station" "fire station" "hotel" "pharmacy" "police station" "restaurant" "square" "train station" "university"]
      "They've | been to" ["airport" "bus station" "fire station" "hotel" "pharmacy" "police station" "restaurant" "square" "train station" "university"]
      "He | hasn't seen" ["airport" "bus station" "fire station" "hotel" "pharmacy" "police station" "restaurant" "square" "train station" "university"]
      "She | hasn't seen" ["airport" "bus station" "fire station" "hotel" "pharmacy" "police station" "restaurant" "square" "train station" "university"]
      "They | haven't seen" ["airport" "bus station" "fire station" "hotel" "pharmacy" "police station" "restaurant" "square" "train station" "university"])
    "Look 4.11.4"
    ("%s %s %s? %s."
      "Have you | visited | a big city" ["Yes, I have" "No, I haven't"]
      "Have you | traveled on | a plane" ["Yes, I have" "No, I haven't"]
      "Have you | walked up | a skyscraper" ["Yes, I have" "No, I haven't"]
      "Have you | seen | a lion" ["Yes, I have" "No, I haven't"]
      "Have you | drawn | a picture" ["Yes, I have" "No, I haven't"]

      "Has he | visited | a big city" ["Yes, he has" "No, he hasn't"]
      "Has he | traveled on | a plane" ["Yes, he has" "No, he hasn't"]
      "Has he | walked up | a skyscraper" ["Yes, he has" "No, he hasn't"]
      "Has he | seen | a lion" ["Yes, he has" "No, he hasn't"]
      "Has he | drawn | a picture" ["Yes, he has" "No, he hasn't"]

      "Has she | visited | a big city" ["Yes, she has" "No, she hasn't"]
      "Has she | traveled on | a plane" ["Yes, she has" "No, she hasn't"]
      "Has she | walked up | a skyscraper" ["Yes, she has" "No, she hasn't"]
      "Has she | seen | a lion" ["Yes, she has" "No, she hasn't"]
      "Has she | drawn | a picture" ["Yes, she has" "No, she hasn't"]

      "Have they | visited | a big city" ["Yes, they have" "No, they haven't"]
      "Have they | traveled on | a plane" ["Yes, they have" "No, they haven't"]
      "Have they | walked up | a skyscraper" ["Yes, they have" "No, they haven't"]
      "Have they | seen | a lion" ["Yes, they have" "No, they haven't"]
      "Have they | drawn | a picture" ["Yes, they have" "No, they haven't"])

    "Look 4.12.2"
    ("%s %s %s? %s."
      "Have you ever | crawled through | an old mine" ["Yes, I have" "No, I haven't"]
      "Have you ever | discovered | a secret cave" ["Yes, I have" "No, I haven't"]
      "Have you ever | gone | kayaking" ["Yes, I have" "No, I haven't"]
      "Have you ever | jumped off | giant steps" ["Yes, I have" "No, I haven't"]
      "Have you ever | smelled | the city" ["Yes, I have" "No, I haven't"]
      "Have you ever | swung | across a river" ["Yes, I have" "No, I haven't"]

      "Has he ever | crawled through | an old mine" ["Yes, he has" "No, he hasn't"]
      "Has he ever | discovered | a secret cave" ["Yes, he has" "No, he hasn't"]
      "Has he ever | gone | kayaking" ["Yes, he has" "No, he hasn't"]
      "Has he ever | jumped off | giant steps" ["Yes, he has" "No, he hasn't"]
      "Has he ever | smelled | the city" ["Yes, he has" "No, he hasn't"]
      "Has he ever | swung | across a river" ["Yes, he has" "No, he hasn't"]

      "Has she ever | crawled through | an old mine" ["Yes, she has" "No, she hasn't"]
      "Has she ever | discovered | a secret cave" ["Yes, she has" "No, she hasn't"]
      "Has she ever | gone | kayaking" ["Yes, she has" "No, she hasn't"]
      "Has she ever | jumped off | giant steps" ["Yes, she has" "No, she hasn't"]
      "Has she ever | smelled | the city" ["Yes, she has" "No, she hasn't"]
      "Has she ever | swung | across a river" ["Yes, she has" "No, she hasn't"]

      "Have they ever | crawled through | an old mine" ["Yes, they have" "No, they haven't"]
      "Have they ever | discovered | a secret cave" ["Yes, they have" "No, they haven't"]
      "Have they ever | gone | kayaking" ["Yes, they have" "No, they haven't"]
      "Have they ever | jumped off | giant steps" ["Yes, they have" "No, they haven't"]
      "Have they ever | smelled | the city" ["Yes, they have" "No, they haven't"]
      "Have they ever | swung | across a river" ["Yes, they have" "No, they haven't"])
    "Look 4.12.4"
    ("%s %s %s."
      "I've | seen | a dolphin"
      "Yesterday, I | saw | a dolphin"
      "I've | walked | a long distance."
      "Yesterday, I | walked | a long distance"
      "I've | been | snorkeling"
      "Yesterday, I | went | snorkeling"
      "I've | jumped off | rocks"
      "Yesterday, I | jumped off | rocks"
      "I've | crawled through | a tunnel"
      "Yesterday, I | crawled through | a tunnel"

      "He's | seen | a dolphin"
      "Yesterday, he | saw | a dolphin"
      "He's | walked | a long distance."
      "Yesterday, he | walked | a long distance"
      "He's | been | snorkeling"
      "Yesterday, he | went | snorkeling"
      "He's | jumped off | rocks"
      "Yesterday, he | jumped off | rocks"
      "He's | crawled through | a tunnel"
      "Yesterday, he | crawled through | a tunnel"

      "She's | seen | a dolphin"
      "Yesterday, she | saw | a dolphin"
      "She's | walked | a long distance."
      "Yesterday, she | walked | a long distance"
      "She's | been | snorkeling"
      "Yesterday, she | went | snorkeling"
      "She's | jumped off | rocks"
      "Yesterday, she | jumped off | rocks"
      "She's | crawled through | a tunnel"
      "Yesterday, she | crawled through | a tunnel")})

(def funcs
  (update-vals data format-gen))
(def lessons (->> funcs keys (sort-by identity util/compare-names)))

(defn line-groups [lesson n]
  (when-let [f (funcs lesson)]
    (->> 100
         range
         (map f)
         distinct
         (filter #(.contains % "_"))
         (partition-all n))))

(defn sentence-pair [lesson i]
  (when-let [f (funcs lesson)]
    (map f (range i (+ i 2)))))
