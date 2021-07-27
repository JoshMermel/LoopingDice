package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class ArrowsMoveValidatorTest : TestCase() {

    private val data = fakeGameCellMetadata()
    private val numRows = 2
    private val numCols = 3
    private val arr = arrayOf("V 1", "H 2", "V 3", "4", "H 5", "6")
    private val board = GameBoard(numRows, numCols, arr, data)

    private val basicFactory = MoveFactory(
        BasicMoveEffect(Axis.HORIZONTAL),
        BasicMoveEffect(Axis.VERTICAL),
        ArrowsValidator()
    )

    private val carouselFactory = MoveFactory(
        CarouselMoveEffect(Axis.HORIZONTAL),
        CarouselMoveEffect(Axis.VERTICAL),
        ArrowsValidator()
    )

    fun testMakeMoveHorizontal() {
        val move = basicFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        val expected = BasicMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveVertical() {
        val move = basicFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveHorizontalIllegal() {
        val move = basicFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board)
        val expected = IllegalMove(listOf(Pair(0, 0), Pair(0, 2)))
        assertEquals(move, expected)
    }

    fun testMakeMoveVerticalIllegal() {
        val move = basicFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board)
        val expected = IllegalMove(listOf(Pair(0, 1), Pair(1, 1)))
        assertEquals(move, expected)
    }

    fun testMakeCarouselMove() {
        // This board would be deadlocked with basic moves but is fine with Carousel moves
        val board = GameBoard(
            2, 2, arrayOf(
                "V 0", "H 1",
                "H 2", "V 3"
            ), data
        )
        val move = carouselFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = CarouselMove(Axis.VERTICAL, Direction.FORWARD, 0, 2, 2)
        assertEquals(move, expected)
    }

    fun testMakeCarouselMoveIllegal() {
        val board = GameBoard(2, 2, arrayOf("V 0", "1", "2", "3"), data)
        // A vertical basic move would be fine here but the carousel move wants to shift (0,0)
        // horizontally so it is illegal
        val move = carouselFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board)
        val expected = IllegalMove(listOf(Pair(0, 0)))
        assertEquals(move, expected)
    }
}