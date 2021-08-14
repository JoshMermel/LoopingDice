package com.joshmermelstein.loopoverplus

class WideMoveEffect(
    private val axis: Axis,
    private val depth: Int,
    metadata: MoveEffectMetadata = MoveEffectMetadata()
) : MoveEffect(metadata) {
    override fun makeMove(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): LegalMove {
        return WideMove(axis, direction, offset, board.numRows, board.numCols, depth)
    }

    // Equality is only used for checking that vertical and horizontal are "the same" so help text
    // can be specialized. As such, we don't look at |axis| in this method.
    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }
        other as WideMoveEffect
        return other.depth == this.depth
    }

    override fun hashCode(): Int {
        return depth
    }
}