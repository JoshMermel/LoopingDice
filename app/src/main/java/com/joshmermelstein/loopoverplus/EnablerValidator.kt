package com.joshmermelstein.loopoverplus

// A factory that returns basic moves, so long as the move includes the enabler cell.
// When it doesn't, returns an illegal moves that flashes a key on the enabler cell(s).
class EnablerValidator : MoveValidator() {
    override fun validate(move: LegalMove, board: GameBoard): Move {
        for (transition in move.transitions) {
            if (board.getCell(transition.y0, transition.x0).family == CellFamily.ENABLER) {
                return move
            }
        }
        return IllegalMove(board.findEnablers())
    }

    override fun helpText(): String {
        return "Moves must contain a gold square"
    }
}