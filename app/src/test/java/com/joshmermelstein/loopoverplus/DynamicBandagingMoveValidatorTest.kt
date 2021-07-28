package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class DynamicBandagingMoveValidatorTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private val numRows = 2
    private val numCols = 3
    private val arr = arrayOf("F 1", "2", "3", "4", "5", "F 6")
    private val board = GameBoard(numRows, numCols, arr, data)

    private val basicFactory = MoveFactory(
        BasicMoveEffect(Axis.HORIZONTAL),
        BasicMoveEffect(Axis.VERTICAL),
        DynamicBandagingValidator()
    )

    fun testMakeMoveHorizontal() {
        val move = basicFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board)
        val expected = BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveVertical() {
        val move = basicFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveHorizontalForwardIllegal() {
        val move = basicFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board)
        val expected = IllegalMove(listOf(Pair(1, 2)))
        assertEquals(move, expected)
    }

    fun testMakeMoveHorizontalBackwardIllegal() {
        val move = basicFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board)
        val expected = IllegalMove(listOf(Pair(0, 0)))
        assertEquals(move, expected)
    }

    fun testMakeMoveVerticalForwardIllegal() {
        val move = basicFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board)
        val expected = IllegalMove(listOf(Pair(1, 2)))
        assertEquals(move, expected)
    }

    fun testMakeMoveVerticalBackwardIllegal() {
        val move = basicFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board)
        val expected = IllegalMove(listOf(Pair(0, 0)))
        assertEquals(move, expected)
    }

    fun testIllegalFromOutOfBounds() {
        val carouselFactory = MoveFactory(
            CarouselMoveEffect(Axis.HORIZONTAL),
            CarouselMoveEffect(Axis.VERTICAL),
            DynamicBandagingValidator()
        )
        val board = GameBoard(
            3, 3, arrayOf(
                "1", "1", "1",
                "1", "1", "1",
                "F 1", "1", "1"
            ), data
        )
        // This move includes a transition from (3,2) to (2,2) which a bugged impl failed to invalidate.
        val move = carouselFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 2, board)
        val expected = IllegalMove(listOf(Pair(2, 3)))
        assertEquals(move, expected)
    }

    // Lightning moves are not valid if the fixed cell ends up off the board, They don't half-execute
    fun testIllegalLightning() {
        val lightningFactory = MoveFactory(
            LightningMoveEffect(Axis.HORIZONTAL),
            LightningMoveEffect(Axis.VERTICAL),
            DynamicBandagingValidator()
        )
        val board = GameBoard(1, 3, arrayOf("L 0", "F 1", "3"), data)

        val move = lightningFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board)
        val expected = IllegalMove(listOf(Pair(0, 1)))
        assertEquals(move, expected)
    }

    // TODO(jmerm): W>1+D test, G+D test, B+D test
}