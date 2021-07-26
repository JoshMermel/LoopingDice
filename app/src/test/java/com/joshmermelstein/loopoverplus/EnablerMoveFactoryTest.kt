package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class EnablerMoveFactoryTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private val numRows = 2
    private val numCols = 3
    private val arr = arrayOf("E", "2", "E", "4", "5", "6")
    private val board = GameBoard(numRows, numCols, arr, data)

    // TODO(jmerm): maybe test with more than basic move effects and rename file to EnablerValidatorTest
    private val factory = MoveFactory(
        BasicMoveEffect(Axis.HORIZONTAL),
        BasicMoveEffect(Axis.VERTICAL),
        EnablerValidator()
    )

    fun testMakeMoveHorizontal() {
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board)
        val expected = BasicMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveVertical() {
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveHorizontalIllegal() {
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        val expected = IllegalMove(listOf(Pair(0, 0), Pair(0, 2)))
        assertEquals(move, expected)
    }

    fun testMakeMoveVerticalIllegal() {
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board)
        val expected = IllegalMove(listOf(Pair(0, 0), Pair(0, 2)))
        assertEquals(move, expected)
    }

    fun testMakeHighlightsHorizontal() {
        val highlights = factory.makeHighlights(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        assertEquals(highlights.size, 1)
        assertEquals(highlights[0], Highlight(Axis.HORIZONTAL, Direction.BACKWARD, 1))
    }

    fun testMakeHighlightsVertical() {
        val highlights = factory.makeHighlights(Axis.VERTICAL, Direction.FORWARD, 0, board)
        assertEquals(highlights.size, 1)
        assertEquals(highlights[0], Highlight(Axis.VERTICAL, Direction.FORWARD, 0))
    }
}