package com.joshmermelstein.loopoverplus

// Basic move moves a single row or column.
// Almost all functionality for manipulating the board comes from the superclass
// so this class gets to be a pretty small specialization of that one.
class BasicMove(
    override val axis: Axis,
    override val direction: Direction,
    override val offset: Int,
    private val numRows: Int,
    private val numCols: Int
) : RowColMove {
    override val transitions = mutableListOf<Transition>()

    init {
        // Fills the |transitions| list with transitions to effect a row/col move.
        when (axis) {
            Axis.HORIZONTAL -> addHorizontal(direction, offset, numCols)
            Axis.VERTICAL -> addVertical(direction, offset, numRows)
        }
    }

    override fun inverse(): LegalMove {
        return BasicMove(axis, opposite(direction), offset, numRows, numCols)
    }

    override fun toString(): String {
        return "BASIC $axis $direction $offset"
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }
        other as BasicMove
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
