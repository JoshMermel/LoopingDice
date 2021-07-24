package com.joshmermelstein.loopoverplus

class WideMoveEffect(private val axis: Axis, private val depth: Int) : MoveEffect {
    override fun makeMove(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): LegalMove {
        return WideMove(axis, direction, offset, board.numRows, board.numCols, depth)
    }

    override fun makeHighlights(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        return Array(depth) {
            Highlight(axis, direction, it + offset)
        }
    }

    override fun helpText(): String {
        return when (axis) {
            Axis.HORIZONTAL -> "Horizontal moves affect $depth " + pluralizedCols(depth)
            Axis.VERTICAL -> "Vertical moves affect $depth " + pluralizedCols(depth)
        }
    }

    override fun helpTextWhenSame(): String {
        return "Vertical and horizontal moves have depth $depth"
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
}

// Returns wide moves according to |rowDepth| and |colDepth|.
class WideMoveFactory(private val rowDepth: Int, private val colDepth: Int) : MoveFactory(
    WideMoveEffect(Axis.HORIZONTAL, rowDepth),
    WideMoveEffect(Axis.VERTICAL, colDepth),
    MoveValidator()
)