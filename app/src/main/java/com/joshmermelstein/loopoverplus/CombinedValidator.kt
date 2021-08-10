package com.joshmermelstein.loopoverplus

// This class combines several MoveValidators into a single moveValidator which has all of their
// limitations. If any input validator rejects a move, the move is deemed illegal. If more than one
// rejects a move, their illegal cells are combined into one IllegalMove so all blockers will flash
// for the user.
// TODO(jmerm): there's a bug here with E+V or E+H cells where they flash a lock when they ought
//  to flash a key. I think I need to make IllegalMove more nuanced and have it hold two lists.
class CombinedValidator(private val validators: List<MoveValidator>, helpText: String = "") :
    MoveValidator(helpText) {
    override fun validate(move: LegalMove, board: GameBoard): Move {
        val illegalCords =
            validators.asSequence().map { it.validate(move, board) }.filterIsInstance<IllegalMove>()
                .map { it.cords }.flatten().distinct().toList()
        return if (illegalCords.isEmpty()) {
            move
        } else {
            IllegalMove(illegalCords)
        }
    }
}