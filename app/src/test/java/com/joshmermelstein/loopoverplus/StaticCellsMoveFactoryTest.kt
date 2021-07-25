package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class StaticCellsMoveFactoryTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private val numRows = 4
    private val numCols = 3
    private val arr = arrayOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        "10", "11", "F 12"
    )
    private val board = GameBoard(numRows, numCols, arr, data)
    // TODO(jmerm): maybe test with more move effects and rename file to StaticCellsValidatorTest
    class StaticCellsMoveFactory(private val rowDepth: Int, private val colDepth: Int) : MoveFactory(
        WideMoveEffect(Axis.HORIZONTAL, rowDepth),
        WideMoveEffect(Axis.VERTICAL, colDepth),
        StaticCellsValidator()
    )

    fun testMakeMoveHorizontal() {
        val factory = StaticCellsMoveFactory(2, 1)
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        val expected = WideMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols, 2)
        assertEquals(move, expected)

        val illegalMove = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, board)
        val expectedIllegal = IllegalMove(listOf(Pair(3, 2)))
        assertEquals(illegalMove, expectedIllegal)
    }

    fun testMakeMoveVertical() {
        val factory = StaticCellsMoveFactory(3, 2)
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = WideMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols, 2)
        assertEquals(move, expected)

        val illegalMove = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board)
        val expectedIllegal = IllegalMove(listOf(Pair(3, 2)))
        assertEquals(illegalMove, expectedIllegal)
    }

    fun testFindLockedCells() {
        val arr = arrayOf("F 1", "F 2", "F 3", "F 4")
        val board = GameBoard(2, 2, arr, data)
        val factory = StaticCellsMoveFactory(2, 2)
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = IllegalMove(listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(1, 1)))
        assertEquals(move, expected)
    }

    fun testMakeHighlightsHorizontal() {
        val factory = StaticCellsMoveFactory(2, 3)
        val highlights = factory.makeHighlights(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        assertEquals(highlights.size, 2)
        assertEquals(highlights[0], Highlight(Axis.HORIZONTAL, Direction.BACKWARD, 1))
        assertEquals(highlights[1], Highlight(Axis.HORIZONTAL, Direction.BACKWARD, 2))
    }

    fun testMakeHighlightsVertical() {
        val factory = StaticCellsMoveFactory(2, 3)
        val highlights = factory.makeHighlights(Axis.VERTICAL, Direction.FORWARD, 1, board)
        assertEquals(highlights.size, 3)
        assertEquals(highlights[0], Highlight(Axis.VERTICAL, Direction.FORWARD, 1))
        assertEquals(highlights[1], Highlight(Axis.VERTICAL, Direction.FORWARD, 2))
        assertEquals(highlights[2], Highlight(Axis.VERTICAL, Direction.FORWARD, 3))
    }
}