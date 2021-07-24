package com.joshmermelstein.loopoverplus

class LightningMoveFactory : MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move {
        return if ((axis == Axis.HORIZONTAL && board.rowContainsLightning(offset)) ||
            (axis == Axis.VERTICAL && board.colContainsLightning(offset))
        ) {
            LightningMove(axis, direction, offset, board.numRows, board.numCols)
        } else {
            BasicMove(axis, direction, offset, board.numRows, board.numCols)
        }
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

    override fun helpText(): String {
        return "Horizontal and vertical moves affect a single row/col"
    }
}