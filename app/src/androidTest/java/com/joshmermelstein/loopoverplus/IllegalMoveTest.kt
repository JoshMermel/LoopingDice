package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class IllegalMoveTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private val numRows = 2
    private val numCols = 2
    private val arr = arrayOf("F 1", "E", "F 2", "E")
    private val board = GameBoard(numRows, numCols, arr, data)
    private val move = IllegalMove(listOf(Pair(0,0), Pair(0,1)))

    fun testTestRunTooSoon() {
        move.run(board, 50, 100, 0)
        // Too early, none fo the cells are modified
        assertFalse(board.getCell(0,0).shouldDrawIcon)
        assertFalse(board.getCell(0,1).shouldDrawIcon)
        assertFalse(board.getCell(1,0).shouldDrawIcon)
        assertFalse(board.getCell(1,1).shouldDrawIcon)
    }

    fun testTestRunTooLate() {
        // Move is complete, all cells return to normal
        move.run(board, 50, 100, 150)
        assertFalse(board.getCell(0,0).shouldDrawIcon)
        assertFalse(board.getCell(0,1).shouldDrawIcon)
        assertFalse(board.getCell(1,0).shouldDrawIcon)
        assertFalse(board.getCell(1,1).shouldDrawIcon)
    }

    fun testTestRunNormally() {
        // In the middle, listed cells are modified
        move.run(board, 50, 100, 75)
        assertTrue(board.getCell(0,0).shouldDrawIcon)
        assertTrue(board.getCell(0,1).shouldDrawIcon)

        assertFalse(board.getCell(1,0).shouldDrawIcon)
        assertFalse(board.getCell(1,1).shouldDrawIcon)
    }
}