package com.joshmermelstein.loopoverplus

// TODO(jmerm): I don't love the name, "Dynamic Bandaging" because of similarity to "Bandaging" mode. Maybe rename?
// Returns basic moves unless the move would slide a fixed cell off the edge of the board.
// In that case returns an illegal moves that flashes a lock on the fixed cell.
class DynamicBandagingValidator : MoveValidator() {
    override fun validate(move: LegalMove, board: GameBoard): Move {
        val illegalTransitions = move.transitions.filter {
            board.isOutOfBounds(it.y1, it.x1) && board.getCell(
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

    // TODO(jmerm): "black" is true in day mode but not in night mode.
    override fun helpText(): String {
        return "Moves cannot push a black square off the edge of the board"
    }
}