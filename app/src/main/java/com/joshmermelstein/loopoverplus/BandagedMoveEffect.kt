package com.joshmermelstein.loopoverplus

// Bandaged moves are like basic moves but they can expand to move additional
// rows/columns depending on the positions of bonds.
class BandagedMoveEffect(
    private val axis: Axis,
    metadata: MoveEffectMetadata = MoveEffectMetadata()
) : MoveEffect(metadata) {
    override fun makeMove(
        direction: Direction,
        offset: Int,
        board: GameBoard
    ): LegalMove {
        val params = applyToBoard(axis, offset, board)
        return WideMove(axis, direction, params.first, board.numRows, board.numCols, params.second)
    }

    // Logic for figuring out which columns move when a particular offset is
    // swiped. Returns a pair, of (Offset, Depth)
    private fun applyToBoard(
        axis: Axis,
        offset: Int,
        board: GameBoard
    ): Pair<Int, Int> {
        var retOffset = offset
        var depth = 1

        if (axis == Axis.HORIZONTAL) {
            // Sweep upward and see if those rows should be included.
            while (board.rowContainsBondUp(retOffset) && depth < board.numRows) {
                retOffset -= 1
                depth += 1
            }
            // Sweep downward and see if those rows should be included.
            while (board.rowContainsBondDown(retOffset + depth - 1) && depth < board.numRows
            ) {
                depth += 1
            }
            // The Move will work correctly with an out of bounds offset but this is also used for
            // user-visible strings so we mod it back into range.
            retOffset = mod(retOffset, board.numRows)
        } else {
            // Sweep left and see if those columns should be included.
            while (board.colContainsBondLeft(retOffset) && depth < board.numCols) {
                retOffset -= 1
                depth += 1
            }
            // Sweep right and see if those columns should be included.
            while (board.colContainsBondRight(retOffset + depth - 1) && depth < board.numCols
            ) {
                depth += 1
            }
            // The Move will work correctly with an out of bounds offset but this is also used for
            // user-visible strings so we mod it back into range.
            retOffset = mod(retOffset, board.numCols)
        }
        return Pair(retOffset, depth)
    }

    // Equality is only used for checking that vertical and horizontal are "the same" so help text
    // can be specialized. As such, we don't look at |axis| in this method.
    override fun equals(other: Any?): Boolean {
        return (javaClass == other?.javaClass)
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}