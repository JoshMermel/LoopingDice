package com.joshmermelstein.loopoverplus

// Wrapper around a 2d array that represents the game board
// getters and setters do modulus operations for safety so callers don't need to worry about
// wrapping out of bounds.
class GameBoard (private val board : Array<Array<GameCell>>) {
    val numRows = board.size
    val numCols = board[0].size

    private fun mod(base : Int, modulus : Int) : Int {
        return ((base % modulus) + modulus) % modulus
    }

    fun getCell(row : Int, col : Int) : GameCell {
        return board[mod(row, numRows)][mod(col, numCols)]
    }

    fun setCell(row : Int, col : Int, cell : GameCell) {
        board[mod(row, numRows)][mod(col, numCols)] = cell
    }

    override fun toString() : String {
        return board.joinToString(",") { row -> row.joinToString(",")}
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