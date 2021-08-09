package com.joshmermelstein.loopoverplus

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