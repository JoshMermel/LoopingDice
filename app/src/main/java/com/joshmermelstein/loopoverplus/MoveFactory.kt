package com.joshmermelstein.loopoverplus

// A move factory translates a swipe into a Move. Each subclass of MoveFactory has different logic
// for which kinds of moves are produced and what validation is done. This lets us implement many
// game modes behind one interface.

// Base class for all move factories.
interface MoveFactory {
    fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move

    // Creates an array of highlights showing which rows/cols would move if the move was executed
    // and is legal.
    fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight>

    fun helpText(): String {
        return verticalHelpText() + "\n" + horizontalHelpText()
    }

    // Human readable explanation of what happens for horizontal and vertical moves.
    fun horizontalHelpText(): String
    fun verticalHelpText(): String
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
        id == "AXISLOCKED" -> {
            return AxisLockedMoveFactory()
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