package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class BasicMoveTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private val numRows = 2
    private val numCols = 3
    private val arr = arrayOf("1", "2", "3", "4", "5", "6")
    private val board = GameBoard(numRows, numCols, arr, data)

    fun testAnimateProgress() {
        val move = BasicMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, numRows, numCols)
        move.animateProgress(0.2, board)

        // A cell in the top row's X offset is updated.
        assertEquals(board.getCell(0, 0).offsetX, -0.2)
        assertEquals(board.getCell(0, 0).offsetY, 0.0)

        // A cell in a different row is not moved.
        assertEquals(board.getCell(1, 0).offsetX, 0.0)
        assertEquals(board.getCell(1, 0).offsetY, 0.0)
    }

    fun testFinalizeHorizontal() {
        val hMove = BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        hMove.finalize(board)

        val expectedArr = arrayOf("3", "1", "2", "4", "5", "6")
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testFinalizeVertical() {
        val vMove = BasicMove(Axis.VERTICAL, Direction.BACKWARD, 1, numRows, numCols)
        vMove.finalize(board)

        val expectedArr = arrayOf("1", "5", "3", "4", "2", "6")
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testToUserString() {
        val hMove = BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(hMove.toUserString(), "Row0")
        val vMove = BasicMove(Axis.VERTICAL, Direction.BACKWARD, 1, numRows, numCols)
        assertEquals(vMove.toUserString(), "Col1'")
    }

    fun testRunTooEarly() {
        val move = BasicMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, numRows, numCols)
        move.run(board, 50, 100, 0)
        assertEquals(board.getCell(0, 0).offsetX, 0.0)
        assertEquals(board.getCell(0, 0).offsetY, 0.0)
    }

    fun testRunPastEndTime() {
        val move = BasicMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, numRows, numCols)
        move.run(board, 50, 100, 150)

        // offsets are all 0
        assertEquals(board.getCell(0, 0).offsetX, 0.0)
        assertEquals(board.getCell(0, 0).offsetY, 0.0)

        // underlying array is updated
        val expectedArr = arrayOf("2", "3", "1", "4", "5", "6")
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testRun() {
        val move = BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        move.run(board, 50, 100, 75)
        assertEquals(board.getCell(0, 0).offsetX, move.ease(0.5))
        assertEquals(board.getCell(0, 0).offsetY, 0.0)
    }

    fun testInverse() {
        val move = BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, 1, 2)
        val expected = BasicMove(Axis.VERTICAL, Direction.BACKWARD, 0, 1, 2)

        val inverse = move.inverse()
        assertEquals(inverse, expected)
    }

    fun testToString() {
        val vMove = BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, 1, 2)
        assertEquals(vMove.toString(), "BASIC V F 0")

        val hMove = BasicMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, 2, 3)
        assertEquals(hMove.toString(), "BASIC H B 1")
    }

    fun testFromString() {
        val numRows = 1
        val numCols = 2

        val vMove = BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(stringToMove(vMove.toString(), numRows, numCols), vMove)

        val hMove = BasicMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols)
        assertEquals(stringToMove(hMove.toString(), numRows, numCols), hMove)
    }
}