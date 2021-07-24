package com.joshmermelstein.loopoverplus

// Returns a carousel move.
// Carousel moves are always legal so this factory doesn't need do any
// validation.
class CarouselMoveEffect(private val axis: Axis) : MoveEffect {
    override fun makeMove(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): LegalMove {
        return CarouselMove(axis, direction, offset, board.numRows, board.numCols)
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
            Axis.HORIZONTAL -> "Horizontal moves are carousel moves"
            Axis.VERTICAL -> "Vertical moves are carousel moves"
        }
    }

    override fun helpTextWhenSame(): String {
        return "Horizontal and vertical moves are carousel moves"
    }

    // Equality is only used for checking that vertical and horizontal are "the same" so help text
    // can be specialized. As such, we don't look at |axis| in this method.
    override fun equals(other: Any?): Boolean {
        return  (javaClass == other?.javaClass)
    }
}

class CarouselMoveFactory : MoveFactory(
    CarouselMoveEffect(Axis.HORIZONTAL),
    CarouselMoveEffect(Axis.VERTICAL),
    MoveValidator()
)
