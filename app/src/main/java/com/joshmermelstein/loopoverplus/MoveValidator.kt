package com.joshmermelstein.loopoverplus

import android.content.Context

// A MoveValidator holds logic for deciding whether a candidate move is valid.
// They are used inside MoveFactory so the same validation logic can be applied to many kind of
// candidate move generate.
open class MoveValidator(private val helpText: String) {
    // Returns either the move or an illegal move.
    // The default impl assumes moves are valid since this is a frequent need.
    open fun validate(move: LegalMove, board: GameBoard): Move = move

    // Prints a description of the validation criteria, suitable for showing to a user.
    open fun helpText(): String = helpText
}

// Factory for move validators
fun makeMoveValidator(id: String, context: Context): MoveValidator {
    // TODO(jmerm): pull help texts out of |context| and pass it to each constructor
    return when (id) {
        "ARROWS" -> ArrowsValidator(context.getString(R.string.arrowValidatorHelptext))
        "DYNAMIC" -> DynamicBandagingValidator(context.getString(R.string.dynamicValidatorHelptext))
        "ENABLER" -> EnablerValidator(context.getString(R.string.enablerValidatorHelptext))
        "NONE" -> MoveValidator("")
        "STATIC" -> StaticCellsValidator(context.getString(R.string.staticValidatorHelptext))
        else -> MoveValidator("")
    }
}