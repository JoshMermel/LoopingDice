package com.joshmermelstein.loopoverplus

// Returns basic moves
// Basic moves are ones where row moves affect 1 row and column moves affect 1 column.
class BasicMoveEffect(private val axis: Axis, metadata: MoveEffectMetadata = MoveEffectMetadata()) :
    MoveEffect(metadata) {
    override fun makeMove(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): LegalMove {
        return BasicMove(axis, direction, offset, board.numRows, board.numCols)
    }

    // Equality is only used for checking that vertical and horizontal are "the same" so help text
    // can be specialized. As such, we don't look at |axis| in this method.
    override fun equals(other: Any?): Boolean {
        return (javaClass == other?.javaClass)
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}