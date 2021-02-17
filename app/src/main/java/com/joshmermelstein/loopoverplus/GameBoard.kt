package com.joshmermelstein.loopoverplus

// Wrapper around a 2d array that represents the game board
// getters and setters do modulus operations for safety so callers don't need to worry about
// wrapping out of bounds.
class GameBoard(private val board: Array<Array<GameCell>>) {
    val numRows = board.size
    val numCols = board[0].size

    // Like % operator but always returns a non-negative number
    private fun mod(base: Int, modulus: Int): Int {
        return ((base % modulus) + modulus) % modulus
    }

    // Gets a cell. Coordinates that are out of range will be modded until they are in range.
    fun getCell(row: Int, col: Int): GameCell {
        return board[mod(row, numRows)][mod(col, numCols)]
    }

    // Sets a cell. Coordinates that are out of range will be modded until they are in range.
    fun setCell(row: Int, col: Int, cell: GameCell) {
        board[mod(row, numRows)][mod(col, numCols)] = cell
    }

    // Returns coordinates of fixed cells in the rectangle define by [left,right)[top,bottom)
    fun findBlockingCells(
        left: Int,
        right: Int,
        top: Int,
        bottom: Int,
    ): List<Pair<Int, Int>> {
        val ret: MutableList<Pair<Int, Int>> = mutableListOf()
        for (col in left until right) {
            for (row in top until bottom) {
                if (getCell(row, col).isBlocking) {
                    ret.add(Pair(row, col))
                }
            }
        }
        return ret
    }

    // Returns a list of the coordinates of all enablers on the board
    fun findEnablers(): List<Pair<Int, Int>> {
        val ret: MutableList<Pair<Int, Int>> = mutableListOf()
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                if (getCell(row, col).isEnabler) {
                    ret.add(Pair(row, col))
                }
            }
        }
        return ret
    }

    // Returns whether any cell in row |offset| has a bond pointing in |bond| direction
    fun rowContainsBond(offset: Int, bond: Bond): Boolean {
        for (col in 0 until numCols) {
            if (getCell(offset, col).bonds().contains(bond)) {
                return true
            }
        }
        return false
    }

    // Returns whether any cell in col |offset| has a bond pointing in |bond| direction
    fun colContainsBond(offset: Int, bond: Bond): Boolean {
        for (row in 0 until numRows) {
            if (getCell(row, offset).bonds().contains(bond)) {
                return true
            }
        }
        return false
    }

    // Returns whether any cell in row |offset| is an enabler cell
    fun rowContainsEnabler(offset: Int): Boolean {
        for (col in 0 until numCols) {
            if (getCell(offset, col).isEnabler) {
                return true
            }
        }
        return false
    }

    // Returns whether any cell in col |offset| is an enabler cell
    fun colContainsEnabler(offset: Int): Boolean {
        for (row in 0 until numRows) {
            if (getCell(row, offset).isEnabler) {
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        return board.joinToString(",") { row -> row.joinToString(",") }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as GameBoard

        for (row in board.indices) {
            for (col in board[row].indices) {
                if (board[row][col] != other.getCell(row, col)) {
                    return false
                }
            }
        }
        return true
    }

    // Makes linter happy since we also override equals
    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + numRows
        result = 31 * result + numCols
        return result
    }
}