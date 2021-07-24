package com.joshmermelstein.loopoverplus

// An IllegalMove represents the outcome of the user swiping on the board where the game's logic
// decided that such a move can't be executed.
// This is a subclass of Move is so it can hook into the moveQueue timing system for animation.
class IllegalMove(private val cords: List<Pair<Int, Int>>) : Move {
    override fun animateProgress(progress: Double, board: GameBoard) {
        for (cord in cords) {
            board.getCell(cord.first, cord.second).shouldDrawIcon = true
        }
    }

    override fun finalize(board: GameBoard) {
        for (cord in cords) {
            board.getCell(cord.first, cord.second).shouldDrawIcon = false
        }
    }

    override fun toString(): String {
        return cords.joinToString(",")
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }
        other as IllegalMove
        return (cords == other.cords)
    }

    override fun hashCode(): Int {
        return cords.hashCode()
    }
}