package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class StaticCellsMoveValidatorTest : TestCase() {
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

    class WideStaticMoveFactory(rowDepth: Int, colDepth: Int) :
        MoveFactory(
            WideMoveEffect(Axis.HORIZONTAL, rowDepth),
            WideMoveEffect(Axis.VERTICAL, colDepth),
            StaticCellsValidator()
        )

    private val carouselStaticMoveFactory = MoveFactory(
        CarouselMoveEffect(Axis.HORIZONTAL),
        CarouselMoveEffect(Axis.VERTICAL),
        StaticCellsValidator()
    )

    // TODO(jmerm): G+S test, B+S test, L+S test

    fun testMakeWideMoveHorizontal() {
        val factory = WideStaticMoveFactory(2, 1)
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        val expected = WideMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols, 2)
        assertEquals(move, expected)

        val illegalMove = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, board)
        val expectedIllegal = IllegalMove(listOf(Pair(3, 2)))
        assertEquals(illegalMove, expectedIllegal)
    }

    fun testMakeWideMoveVertical() {
        val factory = WideStaticMoveFactory(3, 2)
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = WideMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols, 2)
        assertEquals(move, expected)

        val illegalMove = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board)
        val expectedIllegal = IllegalMove(listOf(Pair(3, 2)))
        assertEquals(illegalMove, expectedIllegal)
    }

    fun testMakeCarouselMove() {
        val move = carouselStaticMoveFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board)
        val expected = CarouselMove(Axis.VERTICAL, Direction.BACKWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeCarouselMoveIllegal() {
        val verticalMove = carouselStaticMoveFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board)
        val horizontalMove = carouselStaticMoveFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 2, board)
        val expectedIllegal = IllegalMove(listOf(Pair(3, 2)))

        assertEquals(verticalMove, expectedIllegal)
        assertEquals(horizontalMove, expectedIllegal)
    }

    fun testFindLockedCells() {
        val arr = arrayOf("F 1", "F 2", "F 3", "F 4")
        val board = GameBoard(2, 2, arr, data)
        val factory = WideStaticMoveFactory(2, 2)
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = IllegalMove(listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(1, 1)))
        assertEquals(move, expected)
    }


}