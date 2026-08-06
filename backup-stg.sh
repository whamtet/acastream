cp ../acastream/flashcards3.db .
cp ../acastream/hours.edn .
rsync -av --ignore-existing ../acastream/local .
