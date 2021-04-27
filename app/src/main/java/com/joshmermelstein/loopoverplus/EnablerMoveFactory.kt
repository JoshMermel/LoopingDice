package com.joshmermelstein.loopoverplus

// A factory that returns basic moves, so long as the move includes the enabler cell.
// When it doesn't, returns an illegal moves that flashes a key on the enabler cell(s).
class EnablerMoveFactory : BasicMoveFactory() {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move {
        if (axis == Axis.HORIZONTAL && board.rowContainsEnabler(offset)) {
            return BasicMove(axis, direction, offset, board.numRows, board.numCols)
        } else if (axis == Axis.VERTICAL && board.colContainsEnabler(offset)) {
            return BasicMove(axis, direction, offset, board.numRows, board.numCols)
        }
        return IllegalMove(board.findEnablers())
    }

    override fun helpText(): String {
        return "Moves must contain a gold square"
    }
}