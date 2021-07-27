package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class EnablerMoveValidatorTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private val numRows = 2
    private val numCols = 3
    private val arr = arrayOf("E", "2", "E", "4", "5", "6")
    private val board = GameBoard(numRows, numCols, arr, data)

    private val basicFactory = MoveFactory(
        BasicMoveEffect(Axis.HORIZONTAL),
        BasicMoveEffect(Axis.VERTICAL),
        EnablerValidator()
    )

    fun testMakeMoveHorizontal() {
        val move = basicFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board)
        val expected = BasicMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveVertical() {
        val move = basicFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(move, expected)
    }

    fun testMakeMoveHorizontalIllegal() {
        val move = basicFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        val expected = IllegalMove(listOf(Pair(0, 0), Pair(0, 2)))
        assertEquals(move, expected)
    }

    fun testMakeMoveVerticalIllegal() {
        val move = basicFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board)
        val expected = IllegalMove(listOf(Pair(0, 0), Pair(0, 2)))
        assertEquals(move, expected)
    }

    fun testMakeGearMove() {
        val gearFactory = MoveFactory(
            GearMoveEffect(Axis.HORIZONTAL),
            GearMoveEffect(Axis.VERTICAL),
            EnablerValidator()
        )
        val board = GameBoard(
            3, 3, arrayOf(
                "E", "1", "1",
                "1", "1", "1",
                "1", "1", "1"
            ), data
        )

        // The top row includes the enabler so this move is valid
        val topMove = gearFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board)
        val expectedTop = GearMove(Axis.HORIZONTAL, Direction.FORWARD, 0, 3, 3)
        assertEquals(topMove, expectedTop)

        // Swiping the bottom row effects the top row which includes the enabler so this is valid too
        val bottomMove = gearFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 2, board)
        val expectedBottom = GearMove(Axis.HORIZONTAL, Direction.FORWARD, 2, 3, 3)
        assertEquals(bottomMove, expectedBottom)

        // Swiping the middle row doesn't hit the enable cell so this is not valid.
        val midMove = gearFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board)
        val expectedMid =  IllegalMove(listOf(Pair(0, 0)))
        assertEquals(midMove, expectedMid)
    }
}