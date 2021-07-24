package com.joshmermelstein.loopoverplus

// A move factory translates a swipe into a Move. Each subclass of MoveFactory has different logic
// for which kinds of moves are produced and what validation is done. This lets us implement many
// game modes behind one interface.

// Shared logic for producing and validating moves in response to a user's swipe. To support many
// kinds of move effects and validation schemes (as well as all combination of them), this class
// mostly delegates to MoveEffects and MoveValidators.
open class MoveFactory(
     val rowEffect: MoveEffect,
     val colEffect: MoveEffect,
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
            if (it.isNotEmpty()) { "\n" + it } else { it }
        }

        return moveEffects + validationRules
    }
}

// This is a move factory factory lol
fun makeMoveFactory(id: String): MoveFactory {
    when {
        id.contains("|") -> {
            val args = id.split("|")
            return CombinedMoveFactory(makeMoveFactory(args[0]), makeMoveFactory(args[1]))
        }
        id == "BASIC" -> {
            return BasicMoveFactory()
        }
        id == "GEAR" -> {
            return GearMoveFactory()
        }
        id == "CAROUSEL" -> {
            return CarouselMoveFactory()
        }
        id == "ENABLER" -> {
            return EnablerMoveFactory()
        }
        id == "BANDAGED" -> {
            return BandagedMoveFactory()
        }
        id == "DYNAMIC" -> {
            return DynamicBandagingMoveFactory()
        }
        id == "ARROWS" -> {
            return ArrowsMoveFactory()
        }
        id == "LIGHTNING" -> {
            return LightningMoveFactory()
        }
        id.startsWith("WIDE") -> {
            val args = id.split(" ")
            return WideMoveFactory(args[1].toInt(), args[2].toInt())
        }
        id.startsWith("STATIC") -> {
            val args = id.split(" ")
            return StaticCellsMoveFactory(args[1].toInt(), args[2].toInt())
        }
        else -> return BasicMoveFactory()
    }
}