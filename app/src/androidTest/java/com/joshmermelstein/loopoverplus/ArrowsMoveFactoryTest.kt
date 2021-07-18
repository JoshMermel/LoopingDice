package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class ArrowsMoveFactoryTest : TestCase() {

    private val data = fakeGameCellMetadata()
    private val numRows = 2
    private val numCols = 3
    private val arr = arrayOf("V 1", "H 2", "V 3", "4", "H 5", "6")
    private val board = GameBoard(numRows, numCols, arr, data)
    private val factory = ArrowsMoveFactory()

    fun testMakeMoveHorizontal() {
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        val expected = BasicMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveVertical() {
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveHorizontalIllegal() {
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board)
        val expected = IllegalMove(listOf(Pair(0, 0), Pair(0, 2)))
        assertEquals(move, expected)
    }

    fun testMakeMoveVerticalIllegal() {
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board)
        val expected = IllegalMove(listOf(Pair(0, 1), Pair(1, 1)))
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