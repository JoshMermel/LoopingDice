package com.joshmermelstein.loopoverplus

class LightningMoveEffect(private val axis: Axis, metadata: MoveEffectMetadata) :
    MoveEffect(metadata) {
    override fun makeMove(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): LegalMove {
        return if ((axis == Axis.HORIZONTAL && board.rowContainsLightning(offset)) ||
            (axis == Axis.VERTICAL && board.colContainsLightning(offset))
        ) {
            LightningMove(axis, direction, offset, board.numRows, board.numCols)
        } else {
            BasicMove(axis, direction, offset, board.numRows, board.numCols)
        }
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