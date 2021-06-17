package com.joshmermelstein.loopoverplus

import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase

class BandagedMoveFactoryTest : TestCase() {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    private val factory = BandagedMoveFactory()

    // Tests transitive bonds pushing each other
    fun testMakeMoveHorizontal() {
        val board = GameBoard(
            4,
            2,
            arrayOf("B 1 D", "2", "B 3 U", "B 4 D", "B 5 D", "B 6 U", "B 7 U", "8"),
            appContext
        )

        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 3, board)
        val expected = WideMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, 4, 2, 4)
        assertEquals(move, expected)
    }

    fun testMakeMoveVertical() {
        val board = GameBoard(
            2,
            4,
            arrayOf("B 1 R", "B 2 L", "B 3 R", "B 4 L", "5", "B 6 R", "B 7 L", "8"),
            appContext
        )

        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board)
        val expected = WideMove(Axis.VERTICAL, Direction.FORWARD, 0, 2, 4, 4)
        assertEquals(move, expected)
    }

    // transitively following bonds to see which rows/cols move would loop infinitely. Test that that doesn't happen.
    fun testMakeMoveHorizontalFullWraparound() {
        val board = GameBoard(
            3,
            2,
            arrayOf("B 1 U D", "2", "B 3 U D", "4", "B 5 U D", "6"),
            appContext
        )

        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        val expected = WideMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, 3, 2, 3)
        assertEquals(move, expected)
    }

    fun testMakeMoveVerticalFullWraparound() {
        val board = GameBoard(
            2,
            3,
            arrayOf("B 1 R L", "B 2 R L", "B 3 R L", "4", "5", "6"),
            appContext
        )

        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board)
        val expected = WideMove(Axis.VERTICAL, Direction.FORWARD, 2, 2, 3, 3)
        assertEquals(move, expected)
    }

    fun testMakeHighlightsHorizontal() {
        val board = GameBoard(
            3,
            2,
            arrayOf("B 1 D", "2", "B 3 U", "4", "5", "6"),
            appContext
        )
        val highlights = factory.makeHighlights(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        assertEquals(highlights.size, 2)
        assertEquals(highlights[0], Highlight(Axis.HORIZONTAL, Direction.BACKWARD, 0))
        assertEquals(highlights[1], Highlight(Axis.HORIZONTAL, Direction.BACKWARD, 1))
    }

    fun testMakeHighlightsVertical() {
        val board = GameBoard(
            2,
            3,
            arrayOf("B 1 R", "B 2 L", "3", "4", "5", "6"),
            appContext
        )

        val highlights = factory.makeHighlights(Axis.VERTICAL, Direction.FORWARD, 1, board)
        assertEquals(highlights.size, 2)
        assertEquals(highlights[0], Highlight(Axis.VERTICAL, Direction.FORWARD, 0))
        assertEquals(highlights[1], Highlight(Axis.VERTICAL, Direction.FORWARD, 1))

    }
}