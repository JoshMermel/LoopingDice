package com.joshmermelstein.loopoverplus

import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase

class WideMoveTest : TestCase() {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val numRows = 4
    private val numCols = 3
    private val arr = arrayOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        "10", "11", "12"
    )
    private val board = GameBoard(numRows, numCols, arr, appContext)

    fun testTestFinalizeRowForward() {
        val move = WideMove(Axis.HORIZONTAL, Direction.FORWARD, 2, numRows, numCols, 3)
        move.finalize(board)

        val expectedArr = arrayOf(
            "3", "1", "2",
            "4", "5", "6",
            "9", "7", "8",
            "12", "10", "11"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, appContext)

        assertEquals(board, expectedBoard)
    }

    fun testTestFinalizeRowBackward() {
        val move = WideMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols, 4)
        move.finalize(board)

        val expectedArr = arrayOf(
            "2", "3", "1",
            "5", "6", "4",
            "8", "9", "7",
            "11", "12", "10"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, appContext)

        assertEquals(board, expectedBoard)
    }

    fun testTestFinalizeColForward() {
        val move = WideMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols, 2)
        move.finalize(board)

        val expectedArr = arrayOf(
            "10", "11", "3",
            "1", "2", "6",
            "4", "5", "9",
            "7", "8", "12"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, appContext)

        assertEquals(board, expectedBoard)
    }

    fun testTestFinalizeColBackward() {
        val move = WideMove(Axis.VERTICAL, Direction.BACKWARD, 1, numRows, numCols, 2)
        move.finalize(board)

        val expectedArr = arrayOf(
            "1", "5", "6",
            "4", "8", "9",
            "7", "11", "12",
            "10", "2", "3"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, appContext)

        assertEquals(board, expectedBoard)
    }

    fun testToUserString() {
        val hMove = BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(hMove.toUserString(), "Row0")

        val vMove = BasicMove(Axis.VERTICAL, Direction.BACKWARD, 1, numRows, numCols)
        assertEquals(vMove.toUserString(), "Col1'")
    }

    fun testInverse() {
        val move = WideMove(Axis.VERTICAL, Direction.FORWARD, 0, 1, 2, 4)
        val expected = WideMove(Axis.VERTICAL, Direction.BACKWARD, 0, 1, 2,4)

        val inverse = move.inverse()
        assertEquals(inverse, expected)
    }

    fun testTestToString() {
        val vMove = WideMove(Axis.VERTICAL, Direction.FORWARD, 0, 1, 2,4)
        assertEquals(vMove.toString(), "WIDE V F 0 4")

        val hMove = WideMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, 2, 3, 2)
        assertEquals(hMove.toString(), "WIDE H B 1 2")
    }
}