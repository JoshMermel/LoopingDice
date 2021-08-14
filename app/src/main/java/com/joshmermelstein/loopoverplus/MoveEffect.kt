package com.joshmermelstein.loopoverplus

import android.content.Context

// A MoveEffect is a helper for making moves for either the horizontal or vertical axis of a level.
// MoveEffects work together with MoveValidators in MoveFactories to build and validate moves on
// both axes.
abstract class MoveEffect(private val metadata: MoveEffectMetadata) {
    // Makes a move for the given params, assuming the move is legal
    abstract fun makeMove(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): LegalMove

    // Returns a description of the effect of this move, suitable for displaying to the user.
    fun helpText(): String = metadata.singleAxisHelpText

    // Displays a description of this move and the move on the other axis for use when they are the
    // same. This makes the grammar slightly better.
    fun helpTextWhenSame(): String = metadata.combinedHelpText
}

// Factory for move effects
fun makeMoveEffect(id: String, axis: Axis, context: Context): MoveEffect {
    // Wide effects require extra parsing to split out depth
    if (id.startsWith("WIDE")) {
        val args = id.split(" ")
        val depth = args[1].toInt()
        return WideMoveEffect(axis, depth, makeWideMoveEffectMetadata(depth, axis, context))
    }

    val metadata = makeMoveEffectMetadata(id, axis, context)
    return when (id) {
        "BANDAGED" -> BandagedMoveEffect(axis, metadata)
        "BASIC" -> BasicMoveEffect(axis, metadata)
        "CAROUSEL" -> CarouselMoveEffect(axis, metadata)
        "GEAR" -> GearMoveEffect(axis, metadata)
        "LIGHTNING" -> LightningMoveEffect(axis, metadata)
        else -> BasicMoveEffect(axis, metadata)
    }
}