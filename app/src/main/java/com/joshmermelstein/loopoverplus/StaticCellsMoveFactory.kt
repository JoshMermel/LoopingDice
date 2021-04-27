package com.joshmermelstein.loopoverplus

// Returns wide moves unless those wide moves would slide a fixed cell. In that case returns an
// illegal moves that flashes a lock on the fixed cell(s).
class StaticCellsMoveFactory(override val rowDepth: Int, override val colDepth: Int) :
    WideMoveFactory(rowDepth, colDepth) {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move {
        // Check if any bandaged cells would be moved.
        val staticCellsEncountered: List<Pair<Int, Int>> = when (axis) {
            Axis.HORIZONTAL -> board.findBlockingCells(0, board.numCols, offset, offset + rowDepth)
            Axis.VERTICAL -> board.findBlockingCells(offset, offset + colDepth, 0, board.numRows)
        }

        // If any were found, the move is illegal.
        if (staticCellsEncountered.isNotEmpty()) {
            return IllegalMove(staticCellsEncountered)
        }

        // If not, return a Wide move matching the input.
        return super.makeMove(axis, direction, offset, board)
    }

    override fun helpText(): String {
        return super.helpText() + "\nNeither is allowed to move a black square"
    }
}