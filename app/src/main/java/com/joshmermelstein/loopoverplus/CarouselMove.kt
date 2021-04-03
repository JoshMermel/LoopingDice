package com.joshmermelstein.loopoverplus

// A Carousel move forms a ring with the row/col that was selected and it's neighbor and does a
// circular shift.
open class CarouselMove(
    final override val axis: Axis,
    final override val direction: Direction,
    final override val offset: Int,
    private val numRows: Int,
    private val numCols: Int
) : CoordinatesMove {
    override val transitions = mutableListOf<Transition>()

    init {
        when (axis) {
            Axis.HORIZONTAL -> fillTransitionsHorizontal(direction, offset, numCols)
            Axis.VERTICAL -> fillTransitionsVertical(direction, offset, numRows)
        }
    }

    private fun fillTransitionsHorizontal(
        direction: Direction,
        offset: Int,
        numCols: Int
    ) {
        // Figure out which row goes right and which goes left.
        // Because the Board object mods out-of-range values for us, we don't need to think about
        // that here.
        var rowRightIdx = offset
        var rowLeftIdx = offset
        when (direction) {
            Direction.FORWARD -> rowLeftIdx += 1
            Direction.BACKWARD -> rowRightIdx += 1
        }

        // Move one row right except the rightmost element
        for (col in (numCols - 1 downTo 1)) {
            transitions.add(Transition(col, rowLeftIdx, col - 1, rowLeftIdx))
        }
        // Move the rightmost element into the other row
        transitions.add(Transition(numCols - 1, rowRightIdx, numCols - 1, rowLeftIdx))

        // Move the other row left except the leftmost element
        for (col in (0 until numCols - 1)) {
            transitions.add(Transition(col, rowRightIdx, col + 1, rowRightIdx))
        }
        // Move the leftmost element into the other row
        transitions.add(Transition(0, rowLeftIdx, 0, rowRightIdx))
    }

    private fun fillTransitionsVertical(
        direction: Direction,
        offset: Int,
        numRows: Int
    ) {
        // Figure out which row goes down and which goes up.
        // Because the Board object mods out-of-range values for us, we don't need to think about
        // that here.
        var colDownIdx = offset
        var colUpIdx = offset
        when (direction) {
            Direction.FORWARD -> colUpIdx += 1
            Direction.BACKWARD -> colDownIdx += 1
        }

        // Move all cells in one col down except the bottom one
        for (row in (numRows - 1 downTo 1)) {
            transitions.add(Transition(colUpIdx, row, colUpIdx, row - 1))
        }
        // Move the bottom cell of that col into the other column
        transitions.add(Transition(colDownIdx, numRows - 1, colUpIdx, numRows - 1))

        // Move all cells in the other col up except the top one
        for (row in (0 until numRows - 1)) {
            transitions.add(Transition(colDownIdx, row, colDownIdx, row + 1))
        }
        // Move the top cell of that col into the other column
        transitions.add(Transition(colUpIdx, 0, colDownIdx, 0))
    }

    override fun inverse(): Move {
        return CarouselMove(axis, opposite(direction), offset, numRows, numCols)
    }

    override fun toString(): String {
        return "CAROUSEL $axis $direction $offset"
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }
        other as CarouselMove
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