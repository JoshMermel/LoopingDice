package com.joshmermelstein.loopoverplus

// An Arrows move factory is used in the arrows mode to make sure that arrows game cells
// only move in the ways that their arrows (and underlying types) permit.
// Valid moves are returned as BasicMoves (1 row or 1 column).
// Invalid moves are returned as IllegalMoves which list cells whose lock should flash.
class ArrowsMoveFactory : BasicMoveFactory() {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move {
        // Check if the row/col contains any axis locked cells that could block the move
        val blockers = when (axis) {
            Axis.HORIZONTAL -> board.findColLockedCell(offset)
            Axis.VERTICAL -> board.findRowLockedCell(offset)
        }

        // If any were found, the move is illegal
        if (blockers.isNotEmpty()) {
            return IllegalMove(blockers)
        }

        // Otherwise the move executes as a basic move
        return super.makeMove(axis, direction, offset, board)
    }

    override fun helpText(): String {
        return "Cells containing arrows can only move in those directions."
    }
}