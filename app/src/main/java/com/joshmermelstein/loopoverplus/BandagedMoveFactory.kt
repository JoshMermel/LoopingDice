package com.joshmermelstein.loopoverplus

// Bandaged moves are like basic moves but they can expand to move additional
// rows/columns depending on the positions of bonds.
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

    // Logic for figuring out which columns move when a particular offset is
    // swiped. Returns a pair, of (Offset, Depth)
    private fun applyToBoard(
        axis: Axis,
        offset: Int,
        board: GameBoard
    ): Pair<Int, Int> {
        var retOffset = offset
        var depth = 1

        if (axis == Axis.HORIZONTAL) {
            // Sweep upward and see if those rows should be included.
            while (board.rowContainsBond(retOffset, Bond.UP) && depth < board.numRows) {
                retOffset -= 1
                depth += 1
            }
            // Sweep downward and see if those rows should be included.
            while (board.rowContainsBond(
                    retOffset + depth - 1,
                    Bond.DOWN
                ) && depth < board.numRows
            ) {
                depth += 1
            }
            // The Move will work correctly with an out of bounds offset but this is also used for
            // user-visible strings so we mod it back into range.
            retOffset = mod(retOffset, board.numRows)
        } else {
            // Sweep left and see if those columns should be included.
            while (board.colContainsBond(retOffset, Bond.LEFT) && depth < board.numCols) {
                retOffset -= 1
                depth += 1
            }
            // Sweep right and see if those columns should be included.
            while (board.colContainsBond(
                    retOffset + depth - 1,
                    Bond.RIGHT
                ) && depth < board.numCols
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

        // It's fine if offset goes out of range, it'll get modulus'd into range before being drawn
        return Array(params.second) { idx: Int ->
            Highlight(
                axis,
                direction,
                idx + params.first
            )
        }
    }

    // unused
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
