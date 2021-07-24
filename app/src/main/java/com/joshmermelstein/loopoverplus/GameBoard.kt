package com.joshmermelstein.loopoverplus

// Wrapper around a 2d array that represents the game board
// getters and setters do modulus operations for safety so callers don't need to worry about
// wrapping out of bounds.
class GameBoard(private val board: Array<Array<GameCell>>) {
    val numRows = board.size
    val numCols = board[0].size

    // secondary constructor that creates the board for the caller
    constructor(
        numRows: Int,
        numCols: Int,
        contents: Array<String>,
        data: GameCellMetadata
    ) : this(
        Array(numRows) { row ->
            Array(numCols) { col ->
                makeGameCell(
                    col.toDouble(),
                    row.toDouble(),
                    numRows,
                    numCols,
                    contents[row * numCols + col],
                    data
                )
            }
        })

    // Gets a cell. Coordinates that are out of range will be modded until they are in range.
    fun getCell(row: Int, col: Int): GameCell = board[mod(row, numRows)][mod(col, numCols)]

    // Sets a cell. Coordinates that are out of range will be modded until they are in range.
    fun setCell(row: Int, col: Int, cell: GameCell) {
        board[mod(row, numRows)][mod(col, numCols)] = cell
    }

    // Returns a list of the coordinates of all enablers on the board
    fun findEnablers(): List<Pair<Int, Int>> {
        val ret: MutableList<Pair<Int, Int>> = mutableListOf()
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                if (getCell(row, col).family == CellFamily.ENABLER) {
                    ret.add(Pair(row, col))
                }
            }
        }
        return ret
    }

    // Returns whether any cell in row |offset| has a bond pointing in |bond| direction
    fun rowContainsBond(offset: Int, bond: Bond): Boolean {
        for (col in 0 until numCols) {
            val cell = getCell(offset, col)
            if (cell.family == CellFamily.BANDAGED && (cell as BandagedGameCell).bonds.contains(bond)
            ) {
                return true
            }
        }
        return false
    }

    // Returns whether any cell in col |offset| has a bond pointing in |bond| direction
    fun colContainsBond(offset: Int, bond: Bond): Boolean {
        for (row in 0 until numRows) {
            val cell = getCell(row, offset)
            if (cell.family == CellFamily.BANDAGED && (cell as BandagedGameCell).bonds.contains(bond)
            ) {
                return true
            }
        }
        return false
    }

    // Returns whether any cell in row |offset| is a lightning cell
    fun rowContainsLightning(offset: Int): Boolean {
        for (col in 0 until numCols) {
            if (getCell(offset, col).family == CellFamily.LIGHTNING) {
                return true
            }
        }
        return false
    }

    // Returns whether any cell in col |offset| is a lightning cell
    fun colContainsLightning(offset: Int): Boolean {
        for (row in 0 until numRows) {
            if (getCell(row, offset).family == CellFamily.LIGHTNING) {
                return true
            }
        }
        return false
    }

    // Returns with a coordinate is inside the board
    fun isOutOfBounds(row : Int, col : Int) : Boolean {
        return row !in (0 until numRows) || col !in (0 until numCols)
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