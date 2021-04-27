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
        return arrayOf(
            Highlight(axis, direction, offset),
            Highlight(axis, opposite(direction), offset + 1)
        )
    }

    override fun verticalHelpText(): String {
        return "Vertical moves are gear moves"
    }

    override fun horizontalHelpText(): String {
        return "Horizontal moves are gear moves"
    }

    override fun helpText(): String {
        return "Horizontal and vertical moves are gear moves"
    }
}