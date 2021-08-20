package com.joshmermelstein.loopoverplus

class LightningMove(
    override val axis: Axis,
    override val direction: Direction,
    override val offset: Int,
    private val numRows: Int,
    private val numCols: Int
) : LegalMove {
    override val transitions = mutableListOf<Transition>()

    init {
        // Fills the |transitions| list with transitions to effect a row/col move.
        when (axis) {
            Axis.HORIZONTAL -> addHorizontal(direction, offset, numCols)
            Axis.VERTICAL -> addVertical(direction, offset, numRows)
        }
    }

    private fun addHorizontal(direction: Direction, offset: Int, numCols: Int) {
        val delta = if (direction == Direction.FORWARD) 2 else -2
        for (col in 0 until numCols) {
            transitions.add(Transition(col, offset, col + delta, offset))
        }
    }

    private fun addVertical(direction: Direction, offset: Int, numRows: Int) {
        val delta = if (direction == Direction.FORWARD) 2 else -2
        for (row in 0 until numRows) {
            transitions.add(Transition(offset, row, offset, row + delta))
        }
    }

    override fun inverse(): LegalMove {
        return LightningMove(axis, opposite(direction), offset, numRows, numCols)
    }

    override fun toString(): String {
        return "LIGHTNING $axis $direction $offset"
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }
        other as LightningMove
        return (axis == other.axis) &&
                (direction == other.direction) &&
                (offset == other.offset) &&
                (numRows == other.numRows) &&
                (numCols == other.numCols)
    }

    override fun hashCode(): Int {
        var result = axis.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + offset
        result = 31 * result + numRows
        result = 31 * result + numCols
        return result
    }
}