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
        var rowRightIdx = offset
        var rowLeftIdx = offset
        if (direction == Direction.FORWARD) {
            rowLeftIdx += 1
        } else {
            rowRightIdx += 1
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
        var colDownIdx = offset
        var colUpIdx = offset
        if (direction == Direction.FORWARD) {
            colUpIdx += 1
        } else {
            colDownIdx += 1
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
}