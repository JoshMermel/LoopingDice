package com.joshmermelstein.loopoverplus

// Returns a carousel move.
// Carousel moves are always legal so this factory doesn't need do any
// validation.
class CarouselMoveEffect(
    private val axis: Axis,
    metadata: MoveEffectMetadata = MoveEffectMetadata()
) : MoveEffect(metadata) {
    override fun makeMove(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): LegalMove {
        return CarouselMove(axis, direction, offset, board.numRows, board.numCols)
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