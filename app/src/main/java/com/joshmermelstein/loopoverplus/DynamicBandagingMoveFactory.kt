package com.joshmermelstein.loopoverplus

// TODO(jmerm): this doesn't need to take depths anymore. I'm not making any levels like that.

// Returns wide moves unless those wide moves would slide a fixed cell off the edge of the board.
// In that case returns an illegal moves that flashes a lock on the fixed cell(s).
class DynamicBandagingMoveFactory(override val rowDepth: Int, override val colDepth: Int) :
    WideMoveFactory(rowDepth, colDepth) {

    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move {
        val end = when (direction) {
            Direction.FORWARD -> -1 // Due to wraparound, -1 means "the last row/col"
            Direction.BACKWARD -> 0
        }

        // Check for blocking cells along the edge that could block this move
        val blockingCellsEncountered = when (axis) {
            Axis.HORIZONTAL -> board.findBlockingCells(end, end + 1, offset, offset + rowDepth)
            Axis.VERTICAL -> board.findBlockingCells(offset, offset + colDepth, end, end + 1)
        }

        // If any were found, the move is illegal
        if (blockingCellsEncountered.isNotEmpty()) {
            return IllegalMove(blockingCellsEncountered)
        }

        // If non were found, the move executes like a wide move.
        return super.makeMove(axis, direction, offset, board)
    }

    override fun generalHelpText(): String {
        return "Neither is allowed to move a black square off the edge of the board"
    }
}