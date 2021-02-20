package com.joshmermelstein.loopoverplus

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
        return arrayOf(
            Highlight(axis, direction, offset),
            Highlight(axis, opposite(direction), offset + 1)
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