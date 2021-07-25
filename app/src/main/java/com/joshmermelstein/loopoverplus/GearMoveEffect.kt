package com.joshmermelstein.loopoverplus

// Returns gear moves.
class GearMoveEffect(private val axis: Axis) : MoveEffect {
    override fun makeMove(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): LegalMove {
        return GearMove(axis, direction, offset, board.numRows, board.numCols)
    }

    override fun makeHighlights(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        return arrayOf(
            Highlight(axis, direction, offset),
            Highlight(axis, opposite(direction), offset + 1)
        )
    }

    override fun helpText(): String {
        return when (axis) {
            Axis.HORIZONTAL -> "Horizontal moves are gear moves"
            Axis.VERTICAL -> "Vertical moves are gear moves"
        }
    }

    override fun helpTextWhenSame(): String {
        return "Horizontal and vertical moves are gear moves"
    }

    // Equality is only used for checking that vertical and horizontal are "the same" so help text
    // can be specialized. As such, we don't look at |axis| in this method.
    override fun equals(other: Any?): Boolean {
        return  (javaClass == other?.javaClass)
    }
}