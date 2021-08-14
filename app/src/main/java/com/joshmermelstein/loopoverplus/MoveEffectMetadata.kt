package com.joshmermelstein.loopoverplus

import android.content.Context

// Yet another workaround so I don't have to pass the context around. This class holds the strings
// that a MoveEffect might want to pull out of a Context when its help text methods are called.
data class MoveEffectMetadata(
    val singleAxisHelpText: String,
    val combinedHelpText: String
)

// Factory method for filling in move effect metadata
fun makeMoveEffectMetadata(id: String, axis: Axis, context: Context): MoveEffectMetadata {
    val helpStrings = MoveEffectHelptextMap[id]!!
    return MoveEffectMetadata(
        context.getString(if (axis == Axis.HORIZONTAL) helpStrings[0] else helpStrings[1]),
        context.getString(helpStrings[2])
    )
}

// Wide move effect metadata needs to do some string substitution so it's broken out into this helper.
fun makeWideMoveEffectMetadata(depth: Int, axis: Axis, context: Context): MoveEffectMetadata {
    return MoveEffectMetadata(
        if (axis == Axis.HORIZONTAL) {
            context.getString(R.string.wideRowEffectHelpText, pluralizedRows(depth, context))
        } else {
            context.getString(R.string.wideColEffectHelpText, pluralizedCols(depth, context))
        },
        context.getString(R.string.wideBothEffectHelpText, depth)
    )
}

val MoveEffectHelptextMap = mapOf(
    "BANDAGED" to arrayOf(
        R.string.bandagedRowEffectHelpText,
        R.string.bandagedColEffectHelpText,
        R.string.bandagedBothEffectHelpText
    ),
    "BASIC" to arrayOf(
        R.string.basicRowEffectHelpText,
        R.string.basicColEffectHelpText,
        R.string.basicBothEffectHelpText
    ),
    "CAROUSEL" to arrayOf(
        R.string.carouselRowEffectHelpText,
        R.string.carouselColEffectHelpText,
        R.string.carouselBothEffectHelpText
    ),
    "GEAR" to arrayOf(
        R.string.gearRowEffectHelpText,
        R.string.gearColEffectHelpText,
        R.string.gearBothEffectHelpText
    ),
    "LIGHTNING" to arrayOf(
        R.string.lightningRowEffectHelpText,
        R.string.lightningColEffectHelpText,
        R.string.lightningBothEffectHelpText
    ),
)