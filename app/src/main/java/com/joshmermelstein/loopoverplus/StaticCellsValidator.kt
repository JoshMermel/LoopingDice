package com.joshmermelstein.loopoverplus

// Returns wide moves unless those wide moves would slide a fixed cell. In that case returns an
// illegal moves that flashes a lock on the fixed cell(s).
class StaticCellsValidator : MoveValidator() {
    override fun validate(move: LegalMove, board: GameBoard): Move {
        for (transition in move.transitions) {
            if (board.getCell(transition.y0, transition.x0).family == CellFamily.FIXED) {
                return IllegalMove(move.transitions.map { t -> Pair(t.y0, t.x0) }
                    .filter { board.getCell(it.first, it.second).family == CellFamily.FIXED })
            }
        }
        return move
    }

    // TODO(jmerm): "black" is true in day mode but not in night mode.
    override fun helpText() : String {
        return "black squares prevent moves"
    }
}