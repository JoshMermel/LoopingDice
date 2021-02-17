package com.joshmermelstein.loopoverplus

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
        val modulus = when (axis) {
            Axis.HORIZONTAL -> board.numRows
            Axis.VERTICAL -> board.numCols
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