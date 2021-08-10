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
            // TODO(jmerm): this is a silly way to combine illegal moves. Do better.
            illegalMoves.fold(
                IllegalMove(),
                { accumulator, element ->
                    IllegalMove(
                        (accumulator.lockCords + element.lockCords).distinct(),
                        (accumulator.keyCords + element.keyCords).distinct()
                    )
                })
        }
    }
}