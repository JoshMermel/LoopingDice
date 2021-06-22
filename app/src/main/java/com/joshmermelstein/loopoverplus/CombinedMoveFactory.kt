package com.joshmermelstein.loopoverplus

// Combines two moves factories into one. The first one is used to generate horizontal moves and the
// second one is used to generate vertical moves.
// Many combinations would result in levels that don't really make sense - i.e.
// a level where enabler cells are required for horizontal moves but not
// vertical ones. For best results, I recommend only combining {wide, carousel,
// gear} with one another.
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
}
