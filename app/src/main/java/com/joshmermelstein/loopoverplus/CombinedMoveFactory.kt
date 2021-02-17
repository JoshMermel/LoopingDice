package com.joshmermelstein.loopoverplus

// Combines two moves factories into one. The first one is used to generate horizontal moves and the
// second one is used to generate vertical moves.
class CombinedMoveFactory(private val horizontal: MoveFactory, private val vertical: MoveFactory) :
    MoveFactory {
    override fun makeMove(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Move {
        return when (axis) {
            Axis.HORIZONTAL -> horizontal.makeMove(axis, direction, offset, board)
            Axis.VERTICAL -> vertical.makeMove(axis, direction, offset, board)
        }
    }

    override fun makeHighlights(
        axis: Axis,
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): Array<Highlight> {
        return when (axis) {
            Axis.HORIZONTAL -> horizontal.makeHighlights(axis, direction, offset, board)
            Axis.VERTICAL -> vertical.makeHighlights(axis, direction, offset, board)
        }
    }

    override fun verticalHelpText(): String {
        return vertical.verticalHelpText()
    }

    override fun horizontalHelpText(): String {
        return horizontal.horizontalHelpText()
    }

    // It would be weird to mix modes that have this but maybe not a bug?
    override fun generalHelpText(): String {
        return ""
    }
}