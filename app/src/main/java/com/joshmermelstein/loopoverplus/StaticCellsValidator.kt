package com.joshmermelstein.loopoverplus

// Returns wide moves unless those wide moves would slide a fixed cell. In that case returns an
// illegal moves that flashes a lock on the fixed cell(s).
class StaticCellsValidator(helpText: String = "") : MoveValidator(helpText) {
    override fun validate(move: LegalMove, board: GameBoard): Move {
        // TODO(jmerm): calling isFixed twice here feels unnecessary.
        for (transition in move.transitions) {
            if (board.getCell(transition.y0, transition.x0).isFixed()) {
                return IllegalMove(lockCords = move.transitions.map { t -> Pair(t.y0, t.x0) }
                    .filter { board.getCell(it.first, it.second).isFixed() })
            }
        }
        return move
    }
}