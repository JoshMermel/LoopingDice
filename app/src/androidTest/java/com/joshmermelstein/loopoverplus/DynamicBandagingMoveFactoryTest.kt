package com.joshmermelstein.loopoverplus

import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase

class DynamicBandagingMoveFactoryTest : TestCase() {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val numRows = 2
    private val numCols = 3
    private val arr = arrayOf("F 1", "2", "3", "4", "5", "F 6")
    private val board = GameBoard(numRows, numCols, arr, appContext)
    private val factory = DynamicBandagingMoveFactory()

    fun testMakeMoveHorizontal() {
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board)
        val expected = BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveVertical() {
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveHorizontalForwardIllegal() {
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board)
        val expected = IllegalMove(listOf(Pair(1, -1)))
        assertEquals(move, expected)
    }

    fun testMakeMoveHorizontalBackwardIllegal() {
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board)
        val expected = IllegalMove(listOf(Pair(0, 0)))
        assertEquals(move, expected)
    }

    fun testMakeMoveVerticalForwardIllegal() {
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board)
        val expected = IllegalMove(listOf(Pair(-1, 2)))
        assertEquals(move, expected)
    }

    fun testMakeMoveVerticalBackwardIllegal() {
        val move = factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board)
        val expected = IllegalMove(listOf(Pair(0, 0)))
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