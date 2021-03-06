package com.joshmermelstein.loopoverplus

// Returns basic moves unless the move would slide a fixed cell off the edge of the board.
// In that case returns an illegal moves that flashes a lock on the fixed cell.
class DynamicBandagingMoveFactory : BasicMoveFactory() {

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
            Axis.HORIZONTAL -> board.findBlockingCells(end, end + 1, offset, offset + 1)
            Axis.VERTICAL -> board.findBlockingCells(offset, offset + 1, end, end + 1)
        }

        // If any were found, the move is illegal
        if (blockingCellsEncountered.isNotEmpty()) {
            return IllegalMove(blockingCellsEncountered)
        }

        // If non were found, the move executes like a basic move.
        return super.makeMove(axis, direction, offset, board)
    }

    override fun helpText(): String {
        return "Moves cannot push a black square off the edge of the board"
    }
}
