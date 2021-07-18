package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class CarouselMoveTest : TestCase() {
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
        val move = CarouselMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        move.finalize(board)

        val expectedArr = arrayOf(
            "4", "1", "2",
            "5", "6", "3",
            "7", "8", "9",
            "10", "11", "12"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testTestFinalizeRowBackward() {
        val move = CarouselMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols)
        move.finalize(board)

        val expectedArr = arrayOf(
            "1", "2", "3",
            "5", "6", "9",
            "4", "7", "8",
            "10", "11", "12"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testTestFinalizeColForward() {
        val move = CarouselMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        move.finalize(board)

        val expectedArr = arrayOf(
            "2", "5", "3",
            "1", "8", "6",
            "4", "11", "9",
            "7", "10", "12"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testTestFinalizeColBackward() {
        val move = CarouselMove(Axis.VERTICAL, Direction.BACKWARD, 1, numRows, numCols)
        move.finalize(board)

        val expectedArr = arrayOf(
            "1", "5", "2",
            "4", "8", "3",
            "7", "11", "6",
            "10", "12", "9"
        )
        val expectedBoard = GameBoard(numRows, numCols, expectedArr, data)

        assertEquals(board, expectedBoard)
    }

    fun testAnimateProgressAroundEdge() {
        val arr = arrayOf("1", "2", "3", "4")
        val board = GameBoard(2, 2, arr, data)
        // Swipe down on the right column
        val move = CarouselMove(Axis.VERTICAL, Direction.FORWARD, 1, 2, 2)
        move.animateProgress(0.5, board)

        // The top left cell moves to the left to wrap around
        assertEquals(board.getCell(0, 0).offsetX, -0.5)
        assertEquals(board.getCell(0, 0).offsetY, 0.0)

        // The bottom right cell moves to the right to wrap around
        assertEquals(board.getCell(1, 1).offsetX, 0.5)
        assertEquals(board.getCell(1, 1).offsetY, 0.0)
    }

    fun testToUserString() {
        val hMove = CarouselMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        assertEquals(hMove.toUserString(), "Row0")
        val vMove = CarouselMove(Axis.VERTICAL, Direction.BACKWARD, 1, numRows, numCols)
        assertEquals(vMove.toUserString(), "Col1'")
    }

    fun testInverse() {
        val move = CarouselMove(Axis.VERTICAL, Direction.FORWARD, 0, 1, 2)
        val expected = CarouselMove(Axis.VERTICAL, Direction.BACKWARD, 0, 1, 2)

        val inverse = move.inverse()
        assertEquals(inverse, expected)
    }

    fun testTestToString() {
        val vMove = CarouselMove(Axis.VERTICAL, Direction.FORWARD, 0, 1, 2)
        assertEquals(vMove.toString(), "CAROUSEL V F 0")

        val hMove = CarouselMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, 2, 3)
        assertEquals(hMove.toString(), "CAROUSEL H B 1")
    }
}