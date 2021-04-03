package com.joshmermelstein.loopoverplus

// A gear move is like a basic move but the row/col after the selected one also moves in the
// opposite direction.
class GearMove(
    override val axis: Axis,
    override val direction: Direction,
    override val offset: Int,
    private val numRows: Int,
    private val numCols: Int
) : RowColMove {
    override val transitions = mutableListOf<Transition>()

    init {
        when (axis) {
            // Because the Board object mods out-of-range values for us, we don't need to think
            // about that here.
            Axis.HORIZONTAL -> {
                addHorizontal(direction, offset, numCols)
                addHorizontal(opposite(direction), offset + 1, numCols)
            }
            Axis.VERTICAL -> {
                addVertical(direction, offset, numRows)
                addVertical(opposite(direction), offset + 1, numRows)
            }
        }
    }

    override fun inverse(): Move {
        return GearMove(axis, opposite(direction), offset, numRows, numCols)
    }

    override fun toString(): String {
        return "GEAR $axis $direction $offset"
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }
        other as GearMove
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