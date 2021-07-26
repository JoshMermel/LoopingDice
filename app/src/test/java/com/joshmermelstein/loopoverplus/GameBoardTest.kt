package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class GameBoardTest : TestCase() {
    private val data = fakeGameCellMetadata()
    private var board: GameBoard
    val numRows = 2
    val numCols = 4

    init {
        val arr = arrayOf("1", "E", "F 0", "L 3", "B 1 R", "V 1", "H 2", "314159")
        board = GameBoard(numRows, numCols, arr, data)
    }

    fun testGetCell() {
        // in bounds
        assertEquals(board.getCell(0, 0).colorId, "1")

        // out of bounds to the right
        assertEquals(board.getCell(numRows, 0).colorId, "1")
        assertEquals(board.getCell(2 * numRows, 0).colorId, "1")

        // out of bounds to the left
        assertEquals(board.getCell(-numRows, 0).colorId, "1")
        assertEquals(board.getCell(-2 * numRows, 0).colorId, "1")

        // out of bounds to the bottom
        assertEquals(board.getCell(0, numCols).colorId, "1")
        assertEquals(board.getCell(0, 2 * numCols).colorId, "1")

        // out of bounds to the top
        assertEquals(board.getCell(0, -numCols).colorId, "1")
        assertEquals(board.getCell(0, -2 * numCols).colorId, "1")

        // various diagonals
        assertEquals(board.getCell(numRows, numCols).colorId, "1")
        assertEquals(board.getCell(-numRows, numCols).colorId, "1")
        assertEquals(board.getCell(numRows, -numCols).colorId, "1")
        assertEquals(board.getCell(-numRows, -numCols).colorId, "1")
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
        assertTrue(board.rowContainsBond(1, Bond.RIGHT))
        assertFalse(board.rowContainsBond(1, Bond.LEFT))
        assertFalse(board.rowContainsBond(0, Bond.RIGHT))
    }

    fun testColContainsBond() {
        assertTrue(board.colContainsBond(0, Bond.RIGHT))
        assertFalse(board.colContainsBond(0, Bond.LEFT))
        assertFalse(board.colContainsBond(1, Bond.RIGHT))
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
        assertEquals(board.toString(), "1,E,F 0,L 3,B 1 R,V 1,H 2,314159")
    }

    fun testEquals() {
        val anotherArr = arrayOf("1", "E", "F 0", "L 3", "B 1 R", "V 1", "H 2", "314159")
        val sameBoard = GameBoard(numRows, numCols, anotherArr, data)

        assertEquals(board, sameBoard)
    }

    fun testNotEquals() {
        val arr = arrayOf("2", "E", "F 0", "B 1 R", "5", "6", "L 3", "314159")
        val sameBoard = GameBoard(numRows, numCols, arr, data)

        assertNotSame(board, sameBoard)
    }

}