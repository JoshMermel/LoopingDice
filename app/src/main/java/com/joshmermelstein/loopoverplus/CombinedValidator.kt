package com.joshmermelstein.loopoverplus

// This class combines several MoveValidators into a single moveValidator which has all of their
// limitations. If any input validator rejects a move, the move is deemed illegal. If more than one
// rejects a move, their illegal cells are combined into one IllegalMove so all blockers will flash
// for the user.
class CombinedValidator(private val validators: List<MoveValidator>, helpText: String = "") :
    MoveValidator(helpText) {
    override fun validate(move: LegalMove, board: GameBoard): Move {
        val illegalMoves =
            validators.map { it.validate(move, board) }.filterIsInstance<IllegalMove>()
        return if (illegalMoves.isEmpty()) {
            move
        } else {
            IllegalMove(
                illegalMoves.map { it.lockCords }.flatten().distinct(),
                illegalMoves.map { it.keyCords }.flatten().distinct(),
            )
        }
    }
}