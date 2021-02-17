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

    fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight>

    fun helpText(): String {
        val general = generalHelpText()
        return verticalHelpText() + "\n" + horizontalHelpText() + if (general.isNotEmpty()) {
            "\n" + general
        } else {
            ""
        }
    }

    fun horizontalHelpText(): String
    fun verticalHelpText(): String
    fun generalHelpText(): String
}

// Convenience base class for MoveFactories implementing which return basic moves.
interface BasicMoveFactoryBase : MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move {
        return BasicMove(axis, direction, offset, board.numRows, board.numCols)
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        return arrayOf(Highlight(axis, direction, offset))
    }

    override fun verticalHelpText(): String {
        return "Vertical moves affect a single column"
    }

    override fun horizontalHelpText(): String {
        return "Horizontal moves affect a single row"
    }
}

// Convenience base class for MoveFactories implementing which return wide moves.
interface WideMoveFactoryBase : MoveFactory {
    val rowDepth: Int
    val colDepth: Int

    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move {
        return WideMove(axis, direction, offset, board.numRows, board.numCols, depth(axis))
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        val modulus = when (axis) {
            Axis.HORIZONTAL -> board.numRows
            Axis.VERTICAL -> board.numCols
        }

        return Array(depth(axis)) { idx: Int ->
            Highlight(
                axis,
                direction,
                (idx + offset) % modulus
            )
        }
    }

    fun depth(axis: Axis): Int {
        return when (axis) {
            Axis.HORIZONTAL -> rowDepth
            Axis.VERTICAL -> colDepth
        }
    }

    override fun verticalHelpText(): String {
        return "Vertical moves affect $colDepth " + pluralizedCols(colDepth)
    }

    override fun horizontalHelpText(): String {
        return "Horizontal moves affect $rowDepth " + pluralizedRows(rowDepth)
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
        id.startsWith("WIDE") -> {
            val args = id.split(" ")
            return WideMoveFactory(args[1].toInt(), args[2].toInt())
        }
        id.startsWith("STATIC") -> {
            val args = id.split(" ")
            return StaticCellsMoveFactory(args[1].toInt(), args[2].toInt())
        }
        id.startsWith("DYNAMIC") -> {
            val args = id.split(" ")
            return DynamicBandagingMoveFactory(args[1].toInt(), args[2].toInt())
        }
        else -> return BasicMoveFactory()
    }
}