package com.joshmermelstein.loopoverplus

// A factory that returns basic moves, so long as the move includes the enabler cell.
// When it doesn't, returns an illegal moves that flashes a key on the enabler cell(s).
class EnablerValidator(helpText: String = "") : MoveValidator(helpText) {
    override fun validate(move: LegalMove, board: GameBoard): Move {
        for (transition in move.transitions) {
            if (board.getCell(transition.y0, transition.x0).isEnabler()) {
                return move
            }
        }
        return IllegalMove(keyCords = board.findEnablers())
    }
}