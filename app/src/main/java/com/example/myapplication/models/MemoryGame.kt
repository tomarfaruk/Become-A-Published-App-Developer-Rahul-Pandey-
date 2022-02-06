package com.example.myapplication.models

import com.example.myapplication.utils.DEFAULT_ICON

class MemoryGame(
    private val boardSize: BoardSize,
    private val createdGameImages: List<String>?,
) {
    val cards: List<MemoryCard>
    var numPairsFound = 0

    private var numCardFlips = 0

    private var indexOfSingleSelectedCard: Int? = null

    init {
        if (createdGameImages == null) {
            val chosenImages = DEFAULT_ICON.take(boardSize.getNumPairs())
            val randomizeImages = (chosenImages.shuffled() + chosenImages.shuffled()).shuffled()
            cards = randomizeImages.map { MemoryCard(it) }
        } else {
            val randomizeImages = (createdGameImages + createdGameImages).shuffled()
            cards = randomizeImages.map { MemoryCard(it.hashCode(), it) }
        }
    }

    fun flipCard(position: Int): Boolean {
        numCardFlips++
        val card = cards[position]
        var foundMatch = false
        // Three cases
        // 0 cards previously flipped over => restore cards + flip over the selected card
        // 1 card previously flipped over => flip over the selected card + check if the images match
        // 2 cards previously flipped over => restore cards + flip over the selected card
        if (indexOfSingleSelectedCard == null) {
            restoreCards()
            indexOfSingleSelectedCard = position
        } else {
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }
        card.isFaceUp = !card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if (cards[position1].identifier != cards[position2].identifier) {
            return false
        }
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        return true
    }

    // Turn all unmatched cards face down
    private fun restoreCards() {
        for (card in cards) {
            if (!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    fun haveWonGame(): Boolean {
        return numPairsFound == boardSize.getNumPairs()
    }

    fun isCardFaceUp(position: Int): Boolean {
        return cards[position].isFaceUp
    }

    fun getNumMoves(): Int {
        return numCardFlips / 2
    }

    fun getNumPairs(): Int {
        return numCardFlips / 2

    }
}