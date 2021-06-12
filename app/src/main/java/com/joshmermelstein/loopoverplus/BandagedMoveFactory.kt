package com.joshmermelstein.loopoverplus

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
            while (board.rowContainsBond(retOffset, Bond.UP) && depth <= board.numCols) {
                retOffset -= 1
                depth += 1
            }
            while (board.rowContainsBond(
                    retOffset + depth - 1,
                    Bond.DOWN
                ) && depth <= board.numCols
            ) {
                depth += 1
            }
            // The Move will work correctly with an out of bounds offset but this is also used for
            // user-visible strings so we mod it back into range.
            retOffset = mod(retOffset, board.numRows)
        } else {
            while (board.colContainsBond(retOffset, Bond.LEFT) && depth <= board.numRows) {
                retOffset -= 1
                depth += 1
            }
            while (board.colContainsBond(
                    retOffset + depth - 1,
                    Bond.RIGHT
                ) && depth <= board.numRows
            ) {
                depth += 1
            }
            // The Move will work correctly with an out of bounds offset but this is also used for
            // user-visible strings so we mod it back into range.
            retOffset = mod(retOffset, board.numCols)
        }
        return Pair(retOffset, depth)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        val params = applyToBoard(axis, offset, board)
        val modulus = when (axis) {
            Axis.HORIZONTAL -> board.numRows
            Axis.VERTICAL -> board.numCols
        }

        return Array(params.second) { idx: Int ->
            Highlight(
                axis,
                direction,
                (idx + params.first) % modulus
            )
        }
    }

    // unused?
    override fun verticalHelpText(): String {
        return "Vertical moves are bandaged moves"
    }

    // unused
    override fun horizontalHelpText(): String {
        return "Horizontal moves are bandaged moves"
    }

    override fun helpText(): String {
        return "Blocks connected by a bond always move together and will cause extra rows/columns to be dragged"
    }
}