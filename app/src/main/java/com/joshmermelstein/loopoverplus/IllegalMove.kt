package com.joshmermelstein.loopoverplus

// An IllegalMove represents the outcome of the user swiping on the board where the game's logic
// decided that such a move can't be executed.
// This is a subclass of Move is so it can hook into the moveQueue timing system for animation.
class IllegalMove(
    val lockCords: List<Pair<Int, Int>> = emptyList(),
    val keyCords: List<Pair<Int, Int>> = emptyList()
) : Move {
    override fun animateProgress(progress: Double, board: GameBoard) {
        for (cord in lockCords) {
            board.getCell(cord.first, cord.second).shouldDrawLock = true
        }
        for (cord in keyCords) {
            board.getCell(cord.first, cord.second).shouldDrawKey = true
        }
    }

    override fun finalize(board: GameBoard) {
        for (cord in lockCords) {
            board.getCell(cord.first, cord.second).shouldDrawLock = false
        }
        for (cord in keyCords) {
            board.getCell(cord.first, cord.second).shouldDrawKey = false
        }
    }

    override fun toString(): String {
        return lockCords.joinToString(",") + "::" + keyCords.joinToString(",")
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }
        other as IllegalMove
        return (lockCords == other.lockCords) && (keyCords == other.keyCords)
    }

    override fun hashCode(): Int {
        var result = lockCords.hashCode()
        result = 31 * result + keyCords.hashCode()
        return result
    }
}