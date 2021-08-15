package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class CombinedValidatorTest : TestCase() {
    fun testArrowsDynamic() {
        val factory = MoveFactory(
            BasicMoveEffect(Axis.HORIZONTAL),
            BasicMoveEffect(Axis.VERTICAL),
            CombinedValidator(listOf(ArrowsValidator(), DynamicBlockingValidator()))
        )

        val numRows = 4
        val numCols = 3
        val arr = arrayOf(
            "F 0", "H 1", "2",
            "V 3", "4", "5",
            "6", "7", "H F 8",
            "9", "10", "11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        )
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, board),
            BasicMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(2, 2)))
        )
        // Legal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board),
            BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
    }

    fun testArrowsEnabler() {
        val factory = MoveFactory(
            BasicMoveEffect(Axis.HORIZONTAL),
            BasicMoveEffect(Axis.VERTICAL),
            CombinedValidator(listOf(ArrowsValidator(), EnablerValidator()))
        )

        val numRows = 4
        val numCols = 3
        val arr = arrayOf(
            "V 0", "E 1", "2",
            "3", "4", "E V 5",
            "6", "E H 7", "8",
            "9", "10", "11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 2, board),
            BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 2, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(1, 2)))
        )
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 3, board),
            IllegalMove(keyCords = listOf(Pair(0, 1), Pair(1, 2), Pair(2, 1)))
        )
        // Legal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board),
            BasicMove(Axis.VERTICAL, Direction.FORWARD, 2, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board),
            IllegalMove(keyCords = listOf(Pair(0, 1), Pair(1, 2), Pair(2, 1)))
        )
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(2, 1)))
        )
    }

    fun testArrowsStatic() {
        val factory = MoveFactory(
            BasicMoveEffect(Axis.HORIZONTAL),
            BasicMoveEffect(Axis.VERTICAL),
            CombinedValidator(listOf(ArrowsValidator(), StaticCellsValidator()))
        )

        val numRows = 4
        val numCols = 3
        val arr = arrayOf(
            "F 0", "H 1", "2",
            "V 3", "4", "5",
            "6", "7", "V 8",
            "H 9", "10", "11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())
        // Legal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 3, board),
            BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 3, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(1, 0)))
        )
        // Legal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board),
            BasicMove(Axis.VERTICAL, Direction.FORWARD, 2, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(3, 0), Pair(0, 0)))
        )
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(0, 1)))
        )
    }


    fun testDynamicEnabler() {
        val factory = MoveFactory(
            BasicMoveEffect(Axis.HORIZONTAL),
            BasicMoveEffect(Axis.VERTICAL),
            CombinedValidator(listOf(DynamicBlockingValidator(), EnablerValidator()))
        )

        val numRows = 4
        val numCols = 3
        val arr = arrayOf(
            "F E 0", "1", "F 2",
            "3", "4", "5",
            "F E 6", "7", "8",
            "9", "10", "11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 2, board),
            BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 2, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(2, 0)))
        )
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board),
            IllegalMove(keyCords = listOf(Pair(0, 0), Pair(2, 0)))
        )
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        // Legal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board),
            BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
    }

    fun testEnablerStatic() {
        val factory = MoveFactory(
            BasicMoveEffect(Axis.HORIZONTAL),
            BasicMoveEffect(Axis.VERTICAL),
            CombinedValidator(listOf(EnablerValidator(), StaticCellsValidator()))
        )

        val numRows = 4
        val numCols = 3
        val arr = arrayOf(
            "F 0", "E 1", "2",
            "E 3", "4", "5",
            "6", "7", "8",
            "9", "10", "11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 1, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, board),
            IllegalMove(keyCords = listOf(Pair(0, 1), Pair(1, 0)))
        )
        // Legal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board),
            BasicMove(Axis.VERTICAL, Direction.FORWARD, 1, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 2, board),
            IllegalMove(keyCords = listOf(Pair(0, 1), Pair(1, 0)))
        )
    }

    fun testArrowsEnablerStatic() {
        val factory = MoveFactory(
            BasicMoveEffect(Axis.HORIZONTAL),
            BasicMoveEffect(Axis.VERTICAL),
            CombinedValidator(listOf(ArrowsValidator(), EnablerValidator(), StaticCellsValidator()))
        )

        val numRows = 4
        val numCols = 3
        val arr = arrayOf(
            "F 0", "H 1", "2",
            "V E 3", "4", "V E 5",
            "6", "7", "8",
            "9", "H E 10", "11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 3, board),
            BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 3, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board),
            IllegalMove(
                lockCords = listOf(Pair(0, 0)),
                keyCords = listOf(Pair(1, 0), Pair(1, 2), Pair(3, 1))
            )
        )
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(1, 0), Pair(1,2)))
        )
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, board),
            IllegalMove(keyCords = listOf(Pair(1, 0), Pair(1, 2), Pair(3, 1)))
        )
        // Legal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board),
            BasicMove(Axis.VERTICAL, Direction.FORWARD, 2, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(0, 1), Pair(3,1)))
        )
    }

    fun testArrowsDynamicEnabler() {
        val factory = MoveFactory(
            BasicMoveEffect(Axis.HORIZONTAL),
            BasicMoveEffect(Axis.VERTICAL),
            CombinedValidator(listOf(ArrowsValidator(), DynamicBlockingValidator(), EnablerValidator()))
        )

        val numRows = 4
        val numCols = 3
        val arr = arrayOf(
            "F V 0", "1", "2",
            "E 3", "F 4", "H 5",
            "6", "7", "8",
            "V E 9", "10", "E 11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 1, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board),
            IllegalMove(
                lockCords = listOf(Pair(0, 0)),
                keyCords = listOf(Pair(1, 0), Pair(3, 0), Pair(3, 2))
            )
        )
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, board),
            IllegalMove(keyCords = listOf(Pair(1, 0), Pair(3, 0), Pair(3, 2)))
        )
        assertEquals(
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 3, board),
            IllegalMove(lockCords = listOf(Pair(3, 0)))
        )
        // Legal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board),
            BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(1, 2)))
        )
    }
}