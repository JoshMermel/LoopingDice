package com.joshmermelstein.loopoverplus

// An Arrows move factory is used in the arrows mode to make sure that arrows game cells
// only move in the ways that their arrows (and underlying types) permit.
// Valid moves are returned as BasicMoves (1 row or 1 column).
// Invalid moves are returned as IllegalMoves which list cells whose lock should flash.

class ArrowsValidator(helpText: String = "") : MoveValidator(helpText) {
    override fun validate(move: LegalMove, board: GameBoard): Move {
        val illegalTransitions = move.transitions.filter {
            (board.getCell(it.y0, it.x0).isHoriz() && it.y0 != it.y1) ||
                    (board.getCell(it.y0, it.x0).isVert() && it.x0 != it.x1)
        }.map { t -> Pair(t.y0, t.x0) }
        return if (illegalTransitions.isEmpty()) {
            move
        } else {
            IllegalMove(lockCords = illegalTransitions)
        }
    }
}