package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class GearMoveTest : TestCase() {
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

    fun testTestFinalizeRowForward() {
        val move = GearMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        move.finalize(board)

        val expectedArr = arrayOf(
            "3", "1", "2",
            "5", "6", "4",
            "7", "8", "9",
            "10", "11", "12"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testTestFinalizeRowBackward() {
        val move = GearMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols)
        move.finalize(board)

        val expectedArr = arrayOf(
            "1", "2", "3",
            "5", "6", "4",
            "9", "7", "8",
            "10", "11", "12"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testTestFinalizeColForward() {
        val move = GearMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        move.finalize(board)

        val expectedArr = arrayOf(
            "10", "5", "3",
            "1", "8", "6",
            "4", "11", "9",
            "7", "2", "12"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testTestFinalizeColBackward() {
        val move = GearMove(Axis.VERTICAL, Direction.BACKWARD, 1, numRows, numCols)
        move.finalize(board)

        val expectedArr = arrayOf(
            "1", "5", "12",
            "4", "8", "3",
            "7", "11", "6",
            "10", "2", "9"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testToUserString() {
        val hMove = GearMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(hMove.toUserString(), "Row0")
        val vMove = GearMove(Axis.VERTICAL, Direction.BACKWARD, 1, numRows, numCols)
        assertEquals(vMove.toUserString(), "Col1'")
    }

    fun testInverse() {
        val move = GearMove(Axis.VERTICAL, Direction.FORWARD, 0, 1, 2)
        val expected = GearMove(Axis.VERTICAL, Direction.BACKWARD, 0, 1, 2)

        val inverse = move.inverse()
        assertEquals(inverse, expected)
    }

    fun testToString() {
        val vMove = GearMove(Axis.VERTICAL, Direction.FORWARD, 0, 1, 2)
        assertEquals(vMove.toString(), "GEAR V F 0")

        val hMove = GearMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, 2, 3)
        assertEquals(hMove.toString(), "GEAR H B 1")
    }
}