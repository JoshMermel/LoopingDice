package com.joshmermelstein.loopoverplus

class LightningMoveEffect(private val axis: Axis) : MoveEffect {
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

    override fun helpText(): String {
        return when (axis) {
            Axis.HORIZONTAL -> "Vertical moves which include a lightning bolt travel twice as far"
            Axis.VERTICAL -> "Horizontal moves which include a lightning bolt travel twice as far"
        }
    }

    override fun helpTextWhenSame(): String {
        return "Horizontal and vertical moves which include a lightning bolt travel twice as far"
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