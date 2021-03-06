package com.joshmermelstein.loopoverplus

import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase

class GameBoardTest : TestCase() {
    private var board: GameBoard

    init {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val arr = arrayOf("1", "E", "F 0", "B 1 R", "V 1", "H 2")
        board = GameBoard(2, 3, arr, appContext)
    }

    fun testGetCell() {
        // in bounds
        assertEquals(board.getCell(0, 0).colorId, "1")

        // out of bounds to the right
        assertEquals(board.getCell(2, 0).colorId, "1")
        assertEquals(board.getCell(4, 0).colorId, "1")

        // out of bounds to the left
        assertEquals(board.getCell(-2, 0).colorId, "1")
        assertEquals(board.getCell(-4, 0).colorId, "1")

        // out of bounds to the bottom
        assertEquals(board.getCell(0, 3).colorId, "1")
        assertEquals(board.getCell(0, 6).colorId, "1")

        // out of bounds to the top
        assertEquals(board.getCell(0, -3).colorId, "1")
        assertEquals(board.getCell(0, -6).colorId, "1")

        // various diagonals
        assertEquals(board.getCell(2, 3).colorId, "1")
        assertEquals(board.getCell(-2, 3).colorId, "1")
        assertEquals(board.getCell(2, -3).colorId, "1")
        assertEquals(board.getCell(-2, -3).colorId, "1")
    }

    fun testSetCell() {
        // set in bounds
        board.setCell(0, 0, board.getCell(0, 1))
        assertEquals(board.getCell(0, 0), board.getCell(0, 1))

        // out of bounds right
        board.setCell(0, 0, board.getCell(0, 5))
        assertEquals(board.getCell(0, 0), board.getCell(0, 2))

        // out of bounds left
        board.setCell(0, 0, board.getCell(1, -3))
        assertEquals(board.getCell(0, 0), board.getCell(1, 0))

        // out of bounds bottom
        board.setCell(0, 0, board.getCell(3, 1))
        assertEquals(board.getCell(0, 0), board.getCell(1, 1))

        // out of bounds top
        board.setCell(0, 0, board.getCell(-1, 2))
        assertEquals(board.getCell(0, 0), board.getCell(1, 2))
    }

    fun testFindBlockingCells() {
        // Searching the whole board finds the one blocking cell.
        val blocking = board.findBlockingCells(0, 3, 0, 2)
        assertEquals(blocking.size, 1)
        assertEquals(blocking[0], Pair(0, 2))

        // Searching the bottom row finds no blocking cells.
        val bottomRowBlocking = board.findBlockingCells(0, 3, 1, 2)
        assert(bottomRowBlocking.isEmpty())
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

    fun testRowContainsEnabler() {
        assertTrue(board.rowContainsEnabler(0))
        assertFalse(board.rowContainsEnabler(1))
    }

    fun testColContainsEnabler() {
        assertTrue(board.colContainsEnabler(1))
        assertFalse(board.colContainsEnabler(2))
    }

    fun testFindColLockedCell() {
        val row0 = board.findColLockedCell(0)
        assertEquals(row0.size, 0)

        val row1 = board.findColLockedCell(1)
        assertEquals(row1.size, 1)
        assertEquals(row1[0], Pair(1,1))
    }

    fun testFindRowLockedCell() {
        val col1 = board.findRowLockedCell(1)
        assertEquals(col1.size, 0)

        val col2 = board.findRowLockedCell(2)
        assertEquals(col2.size, 1)
        assertEquals(col2[0], Pair(1,2))
    }

    fun testTestToString() {
        assertEquals(board.toString(), "1,E,F 0,B 1 R,V 1,H 2")
    }

    fun testTestEquals() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val arr = arrayOf("1", "E", "F 0", "B 1 R", "V 1", "H 2")
        val sameBoard = GameBoard(2, 3, arr, appContext)

        assertEquals(board, sameBoard)
    }

    fun testTestNotEquals() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val arr = arrayOf("2", "E", "F 0", "B 1 R", "5", "6")
        val sameBoard = GameBoard(2, 3, arr, appContext)

        assertNotSame(board, sameBoard)
    }
}