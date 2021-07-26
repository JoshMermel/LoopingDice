package com.joshmermelstein.loopoverplus

// Returns basic moves
// Basic moves are ones where row moves affect 1 row and column moves affect 1 column.
class BasicMoveEffect(private val axis: Axis) : MoveEffect {
    override fun makeMove(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): LegalMove {
        return BasicMove(axis, direction, offset, board.numRows, board.numCols)
    }

    override fun makeHighlights(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        return arrayOf(Highlight(axis, direction, offset))
    }

    override fun helpText(): String {
        return when (axis) {
            Axis.HORIZONTAL -> "Horizontal moves affect a single row"
            Axis.VERTICAL -> "Vertical moves affect a single column"
        }
    }

    override fun helpTextWhenSame(): String {
        return "Horizontal and vertical moves affect a single row/col"
    }

    // Equality is only used for checking that vertical and horizontal are "the same" so help text
    // can be specialized. As such, we don't look at |axis| in this method.
    override fun equals(other: Any?): Boolean {
        return (javaClass == other?.javaClass)
    }
}