package com.joshmermelstein.loopoverplus

// Returns basic moves
open class BasicMoveFactory : MoveFactory {
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