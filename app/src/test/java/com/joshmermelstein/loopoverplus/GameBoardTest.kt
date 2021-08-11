package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class GameBoardTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private var board: GameBoard
    val numRows = 2
    val numCols = 4

    init {
        val arr = arrayOf("1", "E", "F 0", "B 3", "R 1", "V 1", "H 2", "D 8")
        board = GameBoard(numRows, numCols, arr, data)
    }

    fun testGetCell() {
        // in bounds
        assertEquals(board.getCell(0, 0).toString(), "1")

        // out of bounds to the right
        assertEquals(board.getCell(numRows, 0).toString(), "1")
        assertEquals(board.getCell(2 * numRows, 0).toString(), "1")

        // out of bounds to the left
        assertEquals(board.getCell(-numRows, 0).toString(), "1")
        assertEquals(board.getCell(-2 * numRows, 0).toString(), "1")

        // out of bounds to the bottom
        assertEquals(board.getCell(0, numCols).toString(), "1")
        assertEquals(board.getCell(0, 2 * numCols).toString(), "1")

        // out of bounds to the top
        assertEquals(board.getCell(0, -numCols).toString(), "1")
        assertEquals(board.getCell(0, -2 * numCols).toString(), "1")

        // various diagonals
        assertEquals(board.getCell(numRows, numCols).toString(), "1")
        assertEquals(board.getCell(-numRows, numCols).toString(), "1")
        assertEquals(board.getCell(numRows, -numCols).toString(), "1")
        assertEquals(board.getCell(-numRows, -numCols).toString(), "1")
    }

    fun testSetCell() {
        // set in bounds
        board.setCell(0, 0, board.getCell(0, 1))
        assertEquals(board.getCell(0, 0), board.getCell(0, 1))

        // out of bounds right
        board.setCell(0, 0, board.getCell(0, 2 + numCols))
        assertEquals(board.getCell(0, 0), board.getCell(0, 2))

        // out of bounds left
        board.setCell(0, 0, board.getCell(1, -numCols))
        assertEquals(board.getCell(0, 0), board.getCell(1, 0))

        // out of bounds bottom
        board.setCell(0, 0, board.getCell(1 + numRows, 1))
        assertEquals(board.getCell(0, 0), board.getCell(1, 1))

        // out of bounds top
        board.setCell(0, 0, board.getCell(1 - numRows, 2))
        assertEquals(board.getCell(0, 0), board.getCell(1, 2))
    }

    fun testFindEnablers() {
        val enablers = board.findEnablers()
        assertEquals(enablers.size, 1)
        assertEquals(enablers[0], Pair(0, 1))
    }

    fun testRowContainsBond() {
        assertTrue(board.rowContainsBondDown(1))
        assertFalse(board.rowContainsBondUp(1))
        assertFalse(board.rowContainsBondDown(0))
    }

    fun testColContainsBond() {
        assertTrue(board.colContainsBondRight(0))
        assertFalse(board.colContainsBondLeft(0))
        assertFalse(board.colContainsBondRight(1))
    }

    fun testRowContainsLightning() {
        assertTrue(board.rowContainsLightning(0))
        assertFalse(board.rowContainsLightning(1))
    }

    fun testColContainsLightning() {
        assertTrue(board.colContainsLightning(3))
        assertFalse(board.colContainsLightning(0))
    }

    fun testToString() {
        assertEquals(board.toString(), "1,E,F 0,B 3,R 1,V 1,H 2,D 8")
    }

    fun testEquals() {
        val anotherArr = arrayOf("1", "E", "F 0", "B 3", "R 1", "V 1", "H 2", "D 8")
        val sameBoard = GameBoard(numRows, numCols, anotherArr, data)

        assertEquals(board, sameBoard)
    }

    fun testNotEquals() {
        val arr = arrayOf("2", "E", "F 0", "R 1", "5", "6", "L 3", "D 8")
        val sameBoard = GameBoard(numRows, numCols, arr, data)

        assertNotSame(board, sameBoard)
    }

}