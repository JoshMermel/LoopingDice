package com.joshmermelstein.loopoverplus

import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase

class CombinedMoveFactoryTest : TestCase() {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val numRows = 2
    private val numCols = 3
    private val arr = arrayOf("1", "2", "3", "4", "5", "6")
    private val board = GameBoard(numRows, numCols, arr, appContext)
    private val factory = CombinedMoveFactory(CarouselMoveFactory(), WideMoveFactory(3, 2))

    fun testMakeMoveHorizontal() {
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        val expected = CarouselMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveVertical() {
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = WideMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols, 2)
        assertEquals(move, expected)
    }

    fun testMakeHighlightsHorizontal() {
        val highlights = factory.makeHighlights(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        assertEquals(highlights.size, 2)
        assertEquals(highlights[0], Highlight(Axis.HORIZONTAL, Direction.BACKWARD, 1))
        assertEquals(highlights[1], Highlight(Axis.HORIZONTAL, Direction.FORWARD, 2))
    }

    fun testMakeHighlightsVertical() {
        val highlights = factory.makeHighlights(Axis.VERTICAL, Direction.FORWARD, 1, board)
        assertEquals(highlights.size, 2)
        assertEquals(highlights[0], Highlight(Axis.VERTICAL, Direction.FORWARD, 1))
        assertEquals(highlights[1], Highlight(Axis.VERTICAL, Direction.FORWARD, 2))
    }
}