package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class LightningMoveTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private val numRows = 5
    private val numCols = 3
    private val arr = arrayOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        "10", "11", "12",
        "13", "14", "15"
    )
    private val board = GameBoard(numRows, numCols, arr, data)

    fun testTestFinalizeRowForward() {
        val move = LightningMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        move.finalize(board)

        val expectedArr = arrayOf(
            "2", "3", "1",
            "4", "5", "6",
            "7", "8", "9",
            "10", "11", "12",
            "13", "14", "15"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testTestFinalizeRowBackward() {
        val move = LightningMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols)
        move.finalize(board)

        val expectedArr = arrayOf(
            "1", "2", "3",
            "6", "4", "5",
            "7", "8", "9",
            "10", "11", "12",
            "13", "14", "15"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testTestFinalizeColForward() {
        val move = LightningMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        move.finalize(board)

        val expectedArr = arrayOf(
            "10", "2", "3",
            "13", "5", "6",
            "1", "8", "9",
            "4", "11", "12",
            "7", "14", "15"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testTestFinalizeColBackward() {
        val move = LightningMove(Axis.VERTICAL, Direction.BACKWARD, 1, numRows, numCols)
        move.finalize(board)

        val expectedArr = arrayOf(
            "1", "8", "3",
            "4", "11", "6",
            "7", "14", "9",
            "10", "2", "12",
            "13", "5", "15"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testToUserString() {
        val hMove = LightningMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(hMove.toUserString(), "Row0")
        val vMove = LightningMove(Axis.VERTICAL, Direction.BACKWARD, 1, numRows, numCols)
        assertEquals(vMove.toUserString(), "Col1'")
    }

    fun testInverse() {
        val move = LightningMove(Axis.VERTICAL, Direction.FORWARD, 0, 1, 2)
        val expected = LightningMove(Axis.VERTICAL, Direction.BACKWARD, 0, 1, 2)

        val inverse = move.inverse()
        assertEquals(inverse, expected)
    }

    fun testToString() {
        val vMove = LightningMove(Axis.VERTICAL, Direction.FORWARD, 0, 1, 2)
        assertEquals(vMove.toString(), "LIGHTNING V F 0")

        val hMove = LightningMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, 2, 3)
        assertEquals(hMove.toString(), "LIGHTNING H B 1")
    }

    fun testFromString() {
        val numRows = 1
        val numCols = 2
        val vMove = LightningMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(stringToMove(vMove.toString(), numRows, numCols), vMove)

        val hMove = LightningMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols)
        assertEquals(stringToMove(hMove.toString(), numRows, numCols), hMove)
    }
}