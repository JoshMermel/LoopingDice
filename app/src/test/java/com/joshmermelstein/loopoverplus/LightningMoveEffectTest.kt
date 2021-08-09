package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class LightningMoveEffectTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private val numRows = 2
    private val numCols = 3
    private val arr = arrayOf(
        "1", "2", "3",
        "4", "B 5", "6",
        "7", "8", "9"
    )
    private val board = GameBoard(numRows, numCols, arr, data)

    private val factory = MoveFactory(
        LightningMoveEffect(Axis.HORIZONTAL),
        LightningMoveEffect(Axis.VERTICAL),
        MoveValidator()
    )

    fun testMakeMoveHorizontalFast() {
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        val expected = LightningMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveHorizontalSlow() {
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board)
        val expected = BasicMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveVerticalFast() {
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board)
        val expected = LightningMove(Axis.VERTICAL, Direction.FORWARD, 1, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveVerticalSlow() {
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    // Highlights don't care about single vs double moves.
    fun testMakeHighlightsHorizontal() {
        // single move
        val highlightsSlow = factory.makeHighlights(Axis.HORIZONTAL, Direction.BACKWARD, 0, board)
        assertEquals(highlightsSlow.size, 1)
        assertEquals(highlightsSlow[0], Highlight(Axis.HORIZONTAL, Direction.BACKWARD, 0))

        // double move
        val highlightsFast = factory.makeHighlights(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        assertEquals(highlightsFast.size, 1)
        assertEquals(highlightsFast[0], Highlight(Axis.HORIZONTAL, Direction.BACKWARD, 1))
    }

    // Highlights don't care about single vs double moves.
    fun testMakeHighlightsVertical() {
        // single move
        val highlightsSlow = factory.makeHighlights(Axis.VERTICAL, Direction.FORWARD, 0, board)
        assertEquals(highlightsSlow.size, 1)
        assertEquals(highlightsSlow[0], Highlight(Axis.VERTICAL, Direction.FORWARD, 0))

        // double move
        val highlightsFast = factory.makeHighlights(Axis.VERTICAL, Direction.FORWARD, 1, board)
        assertEquals(highlightsFast.size, 1)
        assertEquals(highlightsFast[0], Highlight(Axis.VERTICAL, Direction.FORWARD, 1))
    }
}
