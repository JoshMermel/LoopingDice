package com.joshmermelstein.loopoverplus

// Returns wide moves according to |rowDepth| and |colDepth|.
open class WideMoveFactory(open val rowDepth: Int, open val colDepth: Int) :
    MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move {
        return WideMove(axis, direction, offset, board.numRows, board.numCols, depth(axis))
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

        return Array(depth(axis)) { idx: Int ->
            Highlight(
                axis,
                direction,
                (idx + offset) % modulus
            )
        }
    }

    private fun depth(axis: Axis): Int {
        return when (axis) {
            Axis.HORIZONTAL -> rowDepth
            Axis.VERTICAL -> colDepth
        }
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