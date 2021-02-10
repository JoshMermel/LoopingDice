package com.joshmermelstein.loopoverplus

// This is a move factory factory lol
fun makeMoveFactory(id: String): MoveFactory {
    when {
        id.contains("|") -> {
            val args = id.split("|")
            return CombinedMoveFactory(makeMoveFactory(args[0]), makeMoveFactory(args[1]))
        }
        id == "BASIC" -> {
            return BasicMoveFactory()
        }
        id == "GEAR" -> {
            return GearMoveFactory()
        }
        id == "CAROUSEL" -> {
            return CarouselMoveFactory()
        }
        id == "ENABLER" -> {
            return EnablerMoveFactory()
        }
        id == "BANDAGED" -> {
            return BandagedMoveFactory()
        }
        id.startsWith("WIDE") -> {
            val args = id.split(" ")
            return WideMoveFactory(args[1].toInt(), args[2].toInt())
        }
        id.startsWith("STATIC") -> {
            val args = id.split(" ")
            return StaticBandagingMoveFactory(args[1].toInt(), args[2].toInt())
        }
        id.startsWith("DYNAMIC") -> {
            val args = id.split(" ")
            return DynamicBandagingMoveFactory(args[1].toInt(), args[2].toInt())
        }
        else -> return BasicMoveFactory()
    }
}

// A move factory translates a swipe into a Move. Each subclass of MoveFactory has different logic
// for which kinds of moves are produced and what validation is done. This lets us implement many
// game modes behind one interface.
interface MoveFactory {
    fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Move

    fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Array<Highlight> {
        return emptyArray()
    }

    fun helpText(): String {
        val general = generalHelpText()
        return verticalHelpText() + "\n" + horizontalHelpText() + if (general.isNotEmpty()) {
            "\n" + general
        } else {
            ""
        }
    }

    fun horizontalHelpText(): String
    fun verticalHelpText(): String
    fun generalHelpText(): String
}

// Returns basic moves.
class BasicMoveFactory : MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Move {
        return BasicMove(axis, direction, offset)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Array<Highlight> {
        return arrayOf(Highlight(axis, direction, offset))
    }

    override fun verticalHelpText(): String {
        return "Vertical moves affect a single column"
    }

    override fun horizontalHelpText(): String {
        return "Horizontal moves affect a single row"
    }

    override fun generalHelpText(): String {
        return ""
    }
}

// Returns wide moves according to |rowDepth| and |colDepth|.
class WideMoveFactory(private var rowDepth: Int, private var colDepth: Int) : MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Move {
        val depth: Int = if (axis == Axis.HORIZONTAL) {
            rowDepth
        } else {
            colDepth
        }
        return WideMove(axis, direction, offset, depth)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Array<Highlight> {
        val modulus = if (axis == Axis.HORIZONTAL) {
            board.size
        } else {
            board[0].size
        }
        val size = if (axis == Axis.HORIZONTAL) {
            rowDepth
        } else {
            colDepth
        }
        return Array(size) { idx: Int -> Highlight(axis, direction, (idx + offset) % modulus) }
    }

    override fun verticalHelpText(): String {
        return "Vertical moves affect $colDepth " + pluralizedCols(colDepth)
    }

    override fun horizontalHelpText(): String {
        return "Horizontal moves affect $rowDepth " + pluralizedRows(rowDepth)
    }

    override fun generalHelpText(): String {
        return ""
    }
}

// Returns gear moves.
class GearMoveFactory : MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Move {
        return GearMove(axis, direction, offset)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Array<Highlight> {
        val modulus = if (axis == Axis.HORIZONTAL) {
            board.size
        } else {
            board[0].size
        }
        return arrayOf(
            Highlight(axis, direction, offset),
            Highlight(axis, opposite(direction), (offset + 1) % modulus)
        )
    }

    override fun verticalHelpText(): String {
        return "Vertical moves slide a single column but the next column to the right moves the opposite direction"
    }

    override fun horizontalHelpText(): String {
        return "Horizontal moves slide a single row but the row below it slides the opposite direction"
    }

    override fun generalHelpText(): String {
        return ""
    }
}

// Returns wide moves unless those wide moves would slide a bandaged cell. In that case returns an
// illegal moves that flashes a lock on the bandaged cell(s).
class StaticBandagingMoveFactory(private var rowDepth: Int, private var colDepth: Int) :
    MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Move {
        val bandagedCellsEncountered: ArrayList<Pair<Int, Int>> = arrayListOf()
        val numCols: Int = board[0].size
        val numRows: Int = board.size

        if (axis == Axis.HORIZONTAL) {
            for (row in offset until (offset + rowDepth)) {
                for (col in 0 until numCols) {
                    if (board[row % numRows][col].isBandaged) {
                        bandagedCellsEncountered.add(Pair(row % numRows, col))
                    }
                }
            }
        } else {

            for (col in offset until (offset + colDepth)) {
                for (row in 0 until numRows) {
                    if (board[row][col % numCols].isBandaged) {
                        bandagedCellsEncountered.add(Pair(row, col % numCols))
                    }
                }
            }
        }
        if (bandagedCellsEncountered.isNotEmpty()) {
            return IllegalMove(bandagedCellsEncountered)
        }
        val depth: Int = if (axis == Axis.HORIZONTAL) {
            rowDepth
        } else {
            colDepth
        }
        return WideMove(axis, direction, offset, depth)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Array<Highlight> {
        val modulus = if (axis == Axis.HORIZONTAL) {
            board.size
        } else {
            board[0].size
        }
        val size = if (axis == Axis.HORIZONTAL) {
            rowDepth
        } else {
            colDepth
        }
        return Array(size) { idx: Int -> Highlight(axis, direction, (idx + offset) % modulus) }
    }

    override fun verticalHelpText(): String {
        return "Vertical moves affect $colDepth " + pluralizedCols(colDepth)
    }

    override fun horizontalHelpText(): String {
        return "Horizontal moves affect $rowDepth " + pluralizedRows(rowDepth)
    }

    override fun generalHelpText(): String {
        return "Neither is allowed to move a black square"
    }
}

// Returns wide moves unless those wide moves would slide a bandaged cell off the edge of the board.
// In that case returns an illegal moves that flashes a lock on the bandaged cell(s).
class DynamicBandagingMoveFactory(private var rowDepth: Int, private var colDepth: Int) :
    MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Move {
        val bandagedCellsEncountered: ArrayList<Pair<Int, Int>> = arrayListOf()
        val numCols: Int = board[0].size
        val numRows: Int = board.size

        if (axis == Axis.HORIZONTAL) {
            val end = if (direction == Direction.FORWARD) {
                numCols - 1
            } else {
                0
            }
            for (row in offset until (offset + rowDepth)) {
                if (board[row % numRows][end].isBandaged) {
                    bandagedCellsEncountered.add(Pair(row % numRows, end))

                }
            }
        } else {
            val end = if (direction == Direction.FORWARD) {
                numRows - 1
            } else {
                0
            }
            for (col in offset until (offset + colDepth)) {
                if (board[end][col % numCols].isBandaged) {
                    bandagedCellsEncountered.add(Pair(end, col % numCols))
                }
            }
        }
        if (bandagedCellsEncountered.isNotEmpty()) {
            return IllegalMove(bandagedCellsEncountered)
        }
        val depth: Int = if (axis == Axis.HORIZONTAL) {
            rowDepth
        } else {
            colDepth
        }
        return WideMove(axis, direction, offset, depth)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Array<Highlight> {
        val modulus = if (axis == Axis.HORIZONTAL) {
            board.size
        } else {
            board[0].size
        }
        val size = if (axis == Axis.HORIZONTAL) {
            rowDepth
        } else {
            colDepth
        }
        return Array(size) { idx: Int -> Highlight(axis, direction, (idx + offset) % modulus) }
    }

    override fun verticalHelpText(): String {
        return "Vertical moves affect $colDepth " + pluralizedCols(colDepth)
    }

    override fun horizontalHelpText(): String {
        return "Horizontal moves affect $rowDepth " + pluralizedRows(rowDepth)
    }

    override fun generalHelpText(): String {
        return "Neither is allowed to move a black square off the edge of the board"
    }
}

// A factory that returns basic moves, so long as the move includes the enabler cell.
// When it doesn't, returns an illegal moves that flashes a key on the enabler cell.
class EnablerMoveFactory : MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Move {
        if (axis == Axis.HORIZONTAL) {
            val numCols: Int = board[0].size
            for (col in 0 until numCols) {
                if (board[offset][col].isEnabler) {
                    return BasicMove(axis, direction, offset)
                }
            }
        } else {
            val numRows: Int = board.size
            for (row in 0 until numRows) {
                if (board[row][offset].isEnabler) {
                    return BasicMove(axis, direction, offset)
                }
            }
        }
        return IllegalMove(findEnabler(board))
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Array<Highlight> {
        return arrayOf(Highlight(axis, direction, offset))
    }

    private fun findEnabler(board: Array<Array<GameCell>>): List<Pair<Int, Int>> {
        val ret : MutableList<Pair<Int, Int>> = mutableListOf()
        val numRows: Int = board.size
        val numCols: Int = board[0].size
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                if (board[row][col].isEnabler) {
                    ret.add(Pair(row, col))
                }
            }
        }
        return ret
    }

    override fun verticalHelpText(): String {
        return "Vertical moves affect a single column"
    }

    override fun horizontalHelpText(): String {
        return "Horizontal moves affect a single row"
    }

    override fun generalHelpText(): String {
        return "Both must always contain the gold square"
    }
}

// Returns a carousel move.
class CarouselMoveFactory : MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Move {
        return CarouselMove(axis, direction, offset)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Array<Highlight> {
        val modulus = if (axis == Axis.HORIZONTAL) {
            board.size
        } else {
            board[0].size
        }
        return arrayOf(
            Highlight(axis, direction, offset),
            Highlight(axis, opposite(direction), (offset + 1) % modulus)
        )
    }

    override fun verticalHelpText(): String {
        return "Vertical moves form a loop with the column to the right and rotate cells in those two columns"
    }

    override fun horizontalHelpText(): String {
        return "Horizontal moves form a loop with the row below and rotate cells in those two rows"
    }

    override fun generalHelpText(): String {
        return ""
    }
}

// Returns wide moves according to the positions of bonds
class BandagedMoveFactory() : MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Move {
        val params = applyToBoard(axis, direction, offset, board)
        return WideMove(axis, direction, params.first, params.second)
    }

    private fun applyToBoard(axis: Axis,
                             direction: Direction,
                             offset: Int,
                             board: Array<Array<GameCell>>) : Pair<Int, Int> {
        val numRows: Int = board.size
        val numCols: Int = board[0].size

        var retOffset = offset
        var depth = 1

        if (axis == Axis.HORIZONTAL) {
            while (rowContainsBond(retOffset, board, Bond.UP) && depth <= numCols) {
                retOffset -= 1
                depth += 1
            }
            while (rowContainsBond(retOffset + depth - 1, board, Bond.DOWN) && depth <= numCols) {
                depth += 1
            }
            retOffset += numRows
            retOffset %= numRows
        } else {
            while (colContainsBond(retOffset, board, Bond.LEFT) && depth <= numRows) {
                retOffset -= 1
                depth += 1

            }
            while (colContainsBond(retOffset + depth - 1, board, Bond.RIGHT)) {
                depth += 1

            }
            retOffset += numCols
            retOffset %= numCols
        }
        return Pair<Int, Int>(retOffset, depth)
    }

    private fun rowContainsBond(offset: Int, board: Array<Array<GameCell>>, bond: Bond): Boolean {
        val numRows: Int = board.size
        val numCols: Int = board[0].size
        for (col in 0 until numCols) {
            if (board[(offset+numRows) % numRows][col].bonds().contains(bond)) {
                return true
            }
        }
        return false
    }

    private fun colContainsBond(offset: Int, board: Array<Array<GameCell>>, bond: Bond): Boolean {
        val numRows: Int = board.size
        val numCols: Int = board[0].size
        for (row in 0 until numRows) {
            if (board[row][(offset+numCols) % numCols].bonds().contains(bond)) {
                return true
            }
        }
        return false
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Array<Highlight> {
        val params = applyToBoard(axis, direction, offset, board)
        val modulus = if (axis == Axis.HORIZONTAL) {
            board.size
        } else {
            board[0].size
        }

        return Array(params.second) { idx: Int -> Highlight(axis, direction, (idx + params.first) % modulus) }
    }

    override fun verticalHelpText(): String {
        return "Vertical moves affect a single column"
    }

    override fun horizontalHelpText(): String {
        return "Horizontal moves affect a single row"
    }

    // It would be weird to mix modes that have this but maybe not a bug?
    override fun generalHelpText(): String {
        return "Blocks connected by a bond always move together and will cause extra rows/columns to be dragged"
    }
}


// Combines two moves factories into one. The first one is used to generate horizontal moves and the
// second one is used to generate vertical moves.
class CombinedMoveFactory(private val horizontal: MoveFactory, private val vertical: MoveFactory) :
    MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Move {
        return if (axis == Axis.HORIZONTAL) {
            horizontal.makeMove(axis, direction, offset, board)
        } else {
            vertical.makeMove(axis, direction, offset, board)
        }
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: Array<Array<GameCell>>
    ): Array<Highlight> {
        return if (axis == Axis.HORIZONTAL) {
            horizontal.makeHighlights(axis, direction, offset, board)
        } else {
            vertical.makeHighlights(axis, direction, offset, board)
        }
    }

    override fun verticalHelpText(): String {
        return vertical.verticalHelpText()
    }

    override fun horizontalHelpText(): String {
        return horizontal.horizontalHelpText()
    }

    // It would be weird to mix modes that have this but maybe not a bug?
    override fun generalHelpText(): String {
        return ""
    }
}