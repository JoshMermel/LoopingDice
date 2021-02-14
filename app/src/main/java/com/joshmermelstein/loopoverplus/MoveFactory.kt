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
        board: GameBoard
    ): Move

    fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
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
        board: GameBoard
    ): Move {
        return BasicMove(axis, direction, offset, board.numRows, board.numCols)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
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
        board: GameBoard
    ): Move {
        val depth: Int = if (axis == Axis.HORIZONTAL) {
            rowDepth
        } else {
            colDepth
        }
        return WideMove(axis, direction, offset, board.numRows, board.numCols, depth)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        val modulus = if (axis == Axis.HORIZONTAL) { board.numRows } else { board.numCols }
        val size = if (axis == Axis.HORIZONTAL) { rowDepth } else { colDepth }
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
        board: GameBoard
    ): Move {
        return GearMove(axis, direction, offset, board.numRows, board.numCols)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        val modulus = if (axis == Axis.HORIZONTAL) { board.numRows } else { board.numCols }
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
        board: GameBoard
    ): Move {
        val bandagedCellsEncountered: ArrayList<Pair<Int, Int>> = arrayListOf()

        if (axis == Axis.HORIZONTAL) {
            for (row in offset until (offset + rowDepth)) {
                for (col in 0 until board.numCols) {
                    if (board.getCell(row, col).isBandaged) {
                        bandagedCellsEncountered.add(Pair(row % board.numRows, col))
                    }
                }
            }
        } else {

            for (col in offset until (offset + colDepth)) {
                for (row in 0 until board.numRows) {
                    if (board.getCell(row, col).isBandaged) {
                        bandagedCellsEncountered.add(Pair(row, col % board.numCols))
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
        return WideMove(axis, direction, offset, board.numRows, board.numCols, depth)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        val modulus = if (axis == Axis.HORIZONTAL) { board.numRows } else { board.numCols }
        val size = if (axis == Axis.HORIZONTAL) { rowDepth } else { colDepth }
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
        board: GameBoard
    ): Move {
        val bandagedCellsEncountered: ArrayList<Pair<Int, Int>> = arrayListOf()

        if (axis == Axis.HORIZONTAL) {
            val end = if (direction == Direction.FORWARD) { board.numCols - 1 } else { 0 }
            for (row in offset until (offset + rowDepth)) {
                if (board.getCell(row, end).isBandaged) {
                    bandagedCellsEncountered.add(Pair(row % board.numRows, end))

                }
            }
        } else {
            val end = if (direction == Direction.FORWARD) { board.numRows - 1 } else { 0 }
            for (col in offset until (offset + colDepth)) {
                if (board.getCell(end, col).isBandaged) {
                    bandagedCellsEncountered.add(Pair(end, col % board.numCols))
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
        return WideMove(axis, direction, offset, board.numRows, board.numCols, depth)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        val modulus = if (axis == Axis.HORIZONTAL) { board.numRows } else { board.numCols }
        val size = if (axis == Axis.HORIZONTAL) { rowDepth } else { colDepth }
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
        board: GameBoard
    ): Move {
        if (axis == Axis.HORIZONTAL) {
            for (col in 0 until board.numCols) {
                if (board.getCell(offset, col).isEnabler) {
                    return BasicMove(axis, direction, offset, board.numRows, board.numCols)
                }
            }
        } else {
            for (row in 0 until board.numRows) {
                if (board.getCell(row, offset).isEnabler) {
                    return BasicMove(axis, direction, offset, board.numRows, board.numCols)
                }
            }
        }
        return IllegalMove(findEnablers(board))
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        return arrayOf(Highlight(axis, direction, offset))
    }

    // TODO(jmerm): move this and helpers like it into the board class?
    private fun findEnablers(board: GameBoard): List<Pair<Int, Int>> {
        val ret: MutableList<Pair<Int, Int>> = mutableListOf()
        for (row in 0 until board.numRows) {
            for (col in 0 until board.numCols) {
                if (board.getCell(row, col).isEnabler) {
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
        board: GameBoard
    ): Move {
        return CarouselMove(axis, direction, offset, board.numRows, board.numCols)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        val modulus = if (axis == Axis.HORIZONTAL) { board.numRows } else { board.numCols }
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
class BandagedMoveFactory : MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move {
        val params = applyToBoard(axis, offset, board)
        return WideMove(axis, direction, params.first, board.numRows, board.numCols, params.second)
    }

    private fun applyToBoard(
        axis: Axis,
        offset: Int,
        board: GameBoard
    ): Pair<Int, Int> {
        var retOffset = offset
        var depth = 1

        if (axis == Axis.HORIZONTAL) {
            while (rowContainsBond(retOffset, board, Bond.UP) && depth <= board.numCols) {
                retOffset -= 1
                depth += 1
            }
            while (rowContainsBond(retOffset + depth - 1, board, Bond.DOWN) && depth <= board.numCols) {
                depth += 1
            }
            retOffset += board.numRows
            retOffset %= board.numRows
        } else {
            while (colContainsBond(retOffset, board, Bond.LEFT) && depth <= board.numRows) {
                retOffset -= 1
                depth += 1

            }
            while (colContainsBond(retOffset + depth - 1, board, Bond.RIGHT)) {
                depth += 1

            }
            retOffset += board.numCols
            retOffset %= board.numCols
        }
        return Pair(retOffset, depth)
    }

    private fun rowContainsBond(offset: Int, board: GameBoard, bond: Bond): Boolean {
        for (col in 0 until board.numCols) {
            if (board.getCell(offset, col).bonds().contains(bond)) {
                return true
            }
        }
        return false
    }

    private fun colContainsBond(offset: Int, board: GameBoard, bond: Bond): Boolean {
        for (row in 0 until board.numRows) {
            if (board.getCell(row, offset).bonds().contains(bond)) {
                return true
            }
        }
        return false
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        val params = applyToBoard(axis, offset, board)
        val modulus = if (axis == Axis.HORIZONTAL) { board.numRows } else { board.numCols }

        return Array(params.second) { idx: Int ->
            Highlight(
                axis,
                direction,
                (idx + params.first) % modulus
            )
        }
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
        board: GameBoard
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
        board: GameBoard
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