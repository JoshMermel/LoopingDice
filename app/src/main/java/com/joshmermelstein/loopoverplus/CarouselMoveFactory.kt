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
        return "Vertical moves are carousel moves"
    }

    override fun horizontalHelpText(): String {
        return "Horizontal moves are carousel moves"
    }

    override fun helpText(): String {
        return "Horizontal and vertical moves are carousel moves"
    }
}