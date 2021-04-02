package com.joshmermelstein.loopoverplus

// A wide move is like a basic move but it effects many rows/columns depending on depth.
class WideMove(
    override val axis: Axis,
    override val direction: Direction,
    override val offset: Int,
    private val numRows: Int,
    private val numCols: Int,
    private val depth: Int
) : RowColMove {
    override val transitions = mutableListOf<Transition>()

    init {
        // Because the Board object mods out-of-range values for us, we don't need to think about
        // that here.
        when (axis) {
            Axis.HORIZONTAL -> {
                for (row in (offset until offset + depth)) {
                    addHorizontal(direction, row, numCols)
                }
            }
            Axis.VERTICAL -> {
                for (col in (offset until offset + depth)) {
                    addVertical(direction, col, numRows)
                }
            }
        }
    }

    override fun inverse(): Move {
        return WideMove(axis, opposite(direction), offset, numRows, numCols, depth)
    }

    override fun toString(): String {
        return "WIDE $axis $direction $offset $depth"
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }
        other as WideMove
        return (axis == other.axis) &&
                (direction == other.direction) &&
                (offset == other.offset) &&
                (numRows == other.numRows) &&
                (numCols == other.numCols) &&
                (depth == other.depth)
    }
}