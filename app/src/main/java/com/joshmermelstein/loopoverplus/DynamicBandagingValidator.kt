package com.joshmermelstein.loopoverplus

// TODO(jmerm): I don't love the name, "Dynamic Bandaging" because of similarity to "Bandaging" mode. Maybe rename?
// Returns basic moves unless the move would slide a fixed cell off the edge of the board.
// In that case returns an illegal moves that flashes a lock on the fixed cell.
class DynamicBandagingValidator : MoveValidator() {
    override fun validate(move: LegalMove, board: GameBoard): Move {
        val illegalTransitions = move.transitions.filter {
            movesOutOfBounds(it, board.numRows, board.numCols) && board.getCell(
                it.y0,
                it.x0
            ).family == CellFamily.FIXED
        }.map { t -> Pair(t.y0, t.x0) }
        return if (illegalTransitions.isEmpty()) {
            move
        } else {
            IllegalMove(illegalTransitions)
        }
    }

    // Returns whether a transition took a cell out of bounds.
    // This is trickier than just checking whether any destination coordinates are out of bounds
    // because e.g. a wide move on the bottom row might act on a row index that is out of bounds.
    private fun movesOutOfBounds(transition : Transition, numRows : Int, numCols : Int) : Boolean {
        return (transition.y0 == transition.y1 && transition.x1 !in (0 until numCols)) ||
         (transition.x0 == transition.x1 && transition.y1 !in (0 until numRows))
    }

    // TODO(jmerm): "black" is true in day mode but not in night mode.
    override fun helpText(): String {
        return "Moves cannot push a black square off the edge of the board"
    }
}