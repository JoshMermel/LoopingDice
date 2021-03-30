package com.joshmermelstein.loopoverplus

// Basic move moves a single row or column.
class BasicMove(
    override val axis: Axis,
    override val direction: Direction,
    override val offset: Int,
    private val numRows: Int,
    private val numCols: Int
) : RowColMove {
    override val transitions = mutableListOf<Transition>()

    init {
        when (axis) {
            Axis.HORIZONTAL -> addHorizontal(direction, offset, numCols)
            Axis.VERTICAL -> addVertical(direction, offset, numRows)
        }
    }

    override fun inverse(): Move {
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
}