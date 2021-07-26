package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class GearMoveFactoryTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private val numRows = 2
    private val numCols = 3
    private val arr = arrayOf("1", "2", "3", "4", "5", "6")
    private val board = GameBoard(numRows, numCols, arr, data)

    // TODO(jmerm): maybe test with other validators and rename file to GearMoveEffectTest
    private val factory = MoveFactory(
        GearMoveEffect(Axis.HORIZONTAL),
        GearMoveEffect(Axis.VERTICAL),
        MoveValidator()
    )

    fun testMakeMoveHorizontal() {
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        val expected = GearMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveVertical() {
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = GearMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeHighlightsHorizontal() {
        val highlights = factory.makeHighlights(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        assertEquals(highlights.size, 2)
        assertEquals(highlights[0], Highlight(Axis.HORIZONTAL, Direction.BACKWARD, 1))
        assertEquals(highlights[1], Highlight(Axis.HORIZONTAL, Direction.FORWARD, 2))
    }

    fun testMakeHighlightsVertical() {
        val highlights = factory.makeHighlights(Axis.VERTICAL, Direction.FORWARD, 0, board)
        assertEquals(highlights.size, 2)
        assertEquals(highlights[0], Highlight(Axis.VERTICAL, Direction.FORWARD, 0))
        assertEquals(highlights[1], Highlight(Axis.VERTICAL, Direction.BACKWARD, 1))
    }
}