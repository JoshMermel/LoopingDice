package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class IllegalMoveTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private val numRows = 2
    private val numCols = 2
    private val arr = arrayOf("F 1", "E", "F 2", "E")
    private val board = GameBoard(numRows, numCols, arr, data)
    private val move = IllegalMove(lockCords = listOf(Pair(0, 0)), keyCords = listOf(Pair(0, 1)))

    fun testRunTooSoon() {
        move.run(board, 50, 100, 0)
        // Too early, none fo the cells are modified
        assertFalse(board.getCell(0, 0).shouldDrawLock)
        assertFalse(board.getCell(0, 1).shouldDrawKey)
        assertFalse(board.getCell(1, 0).shouldDrawLock)
        assertFalse(board.getCell(1, 1).shouldDrawKey)
    }

    fun testRunTooLate() {
        // Move is complete, all cells return to normal
        move.run(board, 50, 100, 150)
        assertFalse(board.getCell(0, 0).shouldDrawLock)
        assertFalse(board.getCell(0, 1).shouldDrawKey)
        assertFalse(board.getCell(1, 0).shouldDrawLock)
        assertFalse(board.getCell(1, 1).shouldDrawKey)
    }

    fun testRunNormally() {
        // In the middle, listed cells are modified
        move.run(board, 50, 100, 75)
        assertTrue(board.getCell(0, 0).shouldDrawLock)
        assertTrue(board.getCell(0, 1).shouldDrawKey)

        // Non-listed cells are not modified
        assertFalse(board.getCell(1, 0).shouldDrawLock)
        assertFalse(board.getCell(1, 1).shouldDrawKey)

        // Listed cells are only modified in the expected way
        assertFalse(board.getCell(0, 0).shouldDrawKey)
        assertFalse(board.getCell(0, 1).shouldDrawLock)
    }
}