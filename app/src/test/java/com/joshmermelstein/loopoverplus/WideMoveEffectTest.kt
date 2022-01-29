package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class WideMoveEffectTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private val numRows = 2
    private val numCols = 3
    private val arr = arrayOf("1", "2", "3", "4", "5", "6")
    private val board = GameBoard(numRows, numCols, arr, data)

    class WideMoveFactory(rowDepth: Int, colDepth: Int) : MoveFactory(
        WideMoveEffect(Axis.HORIZONTAL, rowDepth),
        WideMoveEffect(Axis.VERTICAL, colDepth),
        StaticCellsValidator()
    )

    fun testMakeMoveHorizontal() {
        val factory = WideMoveFactory(2, 1)
        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        val expected = WideMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols, 2)
        assertEquals(move, expected)
    }

    fun testMakeMoveVertical() {
        val factory = WideMoveFactory(3, 2)
        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board)
        val expected = WideMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols, 2)
        assertEquals(move, expected)
    }
}