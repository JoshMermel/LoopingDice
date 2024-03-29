package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class WideMoveTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private val numRows = 4
    private val numCols = 3
    private val arr = arrayOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        "10", "11", "12"
    )
    private val board = GameBoard(numRows, numCols, arr, data)

    fun testFinalizeRowForward() {
        val move = WideMove(Axis.HORIZONTAL, Direction.FORWARD, 2, numRows, numCols, 3)
        move.finalize(board)

        val expectedArr = arrayOf(
            "3", "1", "2",
            "4", "5", "6",
            "9", "7", "8",
            "12", "10", "11"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testFinalizeRowBackward() {
        val move = WideMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols, 4)
        move.finalize(board)

        val expectedArr = arrayOf(
            "2", "3", "1",
            "5", "6", "4",
            "8", "9", "7",
            "11", "12", "10"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testFinalizeColForward() {
        val move = WideMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols, 2)
        move.finalize(board)

        val expectedArr = arrayOf(
            "10", "11", "3",
            "1", "2", "6",
            "4", "5", "9",
            "7", "8", "12"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testFinalizeColBackward() {
        val move = WideMove(Axis.VERTICAL, Direction.BACKWARD, 1, numRows, numCols, 2)
        move.finalize(board)

        val expectedArr = arrayOf(
            "1", "5", "6",
            "4", "8", "9",
            "7", "11", "12",
            "10", "2", "3"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testToUserString() {
        val hMove = WideMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols, 1)
        assertEquals(hMove.toUserString(), "Row0")

        val vMove = WideMove(Axis.VERTICAL, Direction.BACKWARD, 1, numRows, numCols, 1)
        assertEquals(vMove.toUserString(), "Col1'")
    }

    fun testInverse() {
        val move = WideMove(Axis.VERTICAL, Direction.FORWARD, 0, 1, 2, 4)
        val expected = WideMove(Axis.VERTICAL, Direction.BACKWARD, 0, 1, 2, 4)

        val inverse = move.inverse()
        assertEquals(inverse, expected)
    }

    fun testToString() {
        val vMove = WideMove(Axis.VERTICAL, Direction.FORWARD, 0, 1, 2, 4)
        assertEquals(vMove.toString(), "WIDE V F 0 4")

        val hMove = WideMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, 2, 3, 2)
        assertEquals(hMove.toString(), "WIDE H B 1 2")
    }

    fun testFromString() {
        val numRows = 1
        val numCols = 2
        val vMove = WideMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols, 4)
        assertEquals(stringToMove(vMove.toString(), numRows, numCols), vMove)

        val hMove = WideMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols, 2)
        assertEquals(stringToMove(hMove.toString(), numRows, numCols), hMove)
    }
}