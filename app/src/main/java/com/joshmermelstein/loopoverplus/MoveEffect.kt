package com.joshmermelstein.loopoverplus

// A MoveEffect is a helper for making moves for either the horizontal or vertical axis of a level.
// MoveEffects work together with MoveValidators in MoveFactories to build and validate moves on
// both axes.
interface MoveEffect {
    // Makes a move for the given params, assuming the move is legal
    fun makeMove(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): LegalMove

    // Returns a description of the effect of this move, suitable for displaying to the user.
    fun helpText(): String

    // Displays a description of this move and the move on the other axis for use when they are the
    // same. This makes the grammar slightly better.
    fun helpTextWhenSame(): String
}

// Factory for move effects
fun makeMoveEffect(id: String, axis: Axis): MoveEffect {
    // Wide effects require extra parsing to split out depth
    if (id.startsWith("WIDE")) {
        val args = id.split(" ")
        val depth = args[1].toInt()
        return WideMoveEffect(axis, depth)
    }

    return when (id) {
        "BANDAGED" -> BandagedMoveEffect(axis)
        "BASIC" -> BasicMoveEffect(axis)
        "CAROUSEL" -> CarouselMoveEffect(axis)
        "GEAR" -> GearMoveEffect(axis)
        "LIGHTNING" -> LightningMoveEffect(axis)
        else -> BasicMoveEffect(axis)
    }
}