package com.joshmermelstein.loopoverplus

import android.content.Context

// A MoveValidator holds logic for deciding whether a candidate move is valid.
// They are used inside MoveFactory so the same validation logic can be applied to many kind of
// candidate move generate.
open class MoveValidator {
    // Returns either the move or an illegal move.
    // The default impl assumes moves are valid since this is a frequent need.
    open fun validate(move: LegalMove, board: GameBoard): Move = move

    // Prints a description of the validation criteria, suitable for showing to a user.
    open fun helpText(): String = ""
}

// Factory for move validators
fun makeMoveValidator(id: String, context : Context): MoveValidator {
    // TODO(jmerm): pull help texts out of |context| and pass it to each constructor as a struct
    return when (id) {
        "ARROWS" -> ArrowsValidator()
        "DYNAMIC" -> DynamicBandagingValidator()
        "ENABLER" -> EnablerValidator()
        "NONE" -> MoveValidator()
        "STATIC" -> StaticCellsValidator()
        else -> MoveValidator()
    }
}