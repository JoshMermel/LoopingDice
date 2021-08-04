package com.joshmermelstein.loopoverplus

import android.content.Context

// A move factory translates a swipe into a Move. Each subclass of MoveFactory has different logic
// for which kinds of moves are produced and what validation is done. This lets us implement many
// game modes behind one interface.

// Shared logic for producing and validating moves in response to a user's swipe. To support many
// kinds of move effects and validation schemes (as well as all combination of them), this class
// mostly delegates to MoveEffects and MoveValidators.
open class MoveFactory(
    private val rowEffect: MoveEffect,
    private val colEffect: MoveEffect,
    private val validator: MoveValidator
) {
    fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move {
        return when (axis) {
            Axis.HORIZONTAL -> rowMove(direction, offset, board)
            Axis.VERTICAL -> colMove(direction, offset, board)
        }
    }

    private fun rowMove(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move {
        return validator.validate(rowEffect.makeMove(direction, offset, board), board)
    }

    private fun colMove(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move {
        return validator.validate(colEffect.makeMove(direction, offset, board), board)
    }

    // Creates an array of highlights showing which rows/cols would move if the move was executed
    // and is legal.
    fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        return when (axis) {
            Axis.HORIZONTAL -> rowEffect.makeHighlights(direction, offset, board)
            Axis.VERTICAL -> colEffect.makeHighlights(direction, offset, board)
        }
    }

    fun helpText(): String {
        val moveEffects = if (rowEffect == colEffect) {
            rowEffect.helpTextWhenSame()
        } else {
            rowEffect.helpText() + "\n" + colEffect.helpText()
        }
        val validationRules = validator.helpText().let {
            if (it.isNotEmpty()) {
                "\n" + it
            } else {
                it
            }
        }

        return moveEffects + validationRules
    }
}

// This is a move factory factory lol
fun makeMoveFactory(id: String, context: Context): MoveFactory {
    return id.split("|").let {
        // TODO(jmerm): handle case of fewer then 3 parts.
        MoveFactory(
            makeMoveEffect(it[0], Axis.HORIZONTAL, context),
            makeMoveEffect(it[1], Axis.VERTICAL, context),
            makeMoveValidator(it[2])
        )
    }
}