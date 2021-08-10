package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class ArrowsMoveValidatorTest : TestCase() {
    fun testMakeBasicArrowsMove() {
        val basicFactory = MoveFactory(
            BasicMoveEffect(Axis.HORIZONTAL),
            BasicMoveEffect(Axis.VERTICAL),
            ArrowsValidator()
        )
        val numRows = 2
        val numCols = 3
        val arr = arrayOf(
            "H 0", "1", "2",
            "3", "4", "V 5"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            basicFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            basicFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(1, 2)))
        )
        // Legal col
        assertEquals(
            basicFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board),
            BasicMove(Axis.VERTICAL, Direction.FORWARD, 2, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            basicFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
    }

    fun testMakeBandagedArrowsMove() {
        val bandagedFactory = MoveFactory(
            BandagedMoveEffect(Axis.HORIZONTAL),
            BandagedMoveEffect(Axis.VERTICAL),
            ArrowsValidator()
        )

        val numRows = 3
        val numCols = 4
        val arr = arrayOf(
            "B 0 R", "B 1 L", "H 2", "3",
            "V 4", "5", "B 6 D R", "B 7 D L",
            "8", "9", "B 10 U R", "B 11 U L"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            bandagedFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            WideMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols, 1)
        )
        // Illegal row
        assertEquals(
            bandagedFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(1, 0)))
        )
        // Legal col
        assertEquals(
            bandagedFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board),
            WideMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols, 2)
        )
        // Illegal col
        assertEquals(
            bandagedFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 3, board),
            IllegalMove(lockCords = listOf(Pair(0, 2)))
        )
    }

    fun testMakeCarouselArrowsMove() {
        val carouselFactory = MoveFactory(
            CarouselMoveEffect(Axis.HORIZONTAL),
            CarouselMoveEffect(Axis.VERTICAL),
            ArrowsValidator()
        )
        val numRows = 3
        val numCols = 4
        val arr = arrayOf(
            "0", "1", "2", "H 3",
            "V 4", "5", "6", "7",
            "8", "9", "10", "V 11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            carouselFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board),
            CarouselMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols)
        )
        assertEquals(
            carouselFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 2, board),
            CarouselMove(Axis.HORIZONTAL, Direction.FORWARD, 2, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            carouselFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 3)))
        )
        assertEquals(
            carouselFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(1, 0)))
        )
        // Legal col
        assertEquals(
            carouselFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board),
            CarouselMove(Axis.VERTICAL, Direction.FORWARD, 2, numRows, numCols)
        )
        assertEquals(
            carouselFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 3, board),
            CarouselMove(Axis.VERTICAL, Direction.BACKWARD, 3, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            carouselFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(2, 3), Pair(0, 3)))
        )
        assertEquals(
            carouselFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 3, board),
            IllegalMove(lockCords = listOf(Pair(2, 3), Pair(0, 3)))
        )
    }

    fun testMakeGearArrowsMove() {
        val gearFactory = MoveFactory(
            GearMoveEffect(Axis.HORIZONTAL),
            GearMoveEffect(Axis.VERTICAL),
            ArrowsValidator()
        )
        val numRows = 3
        val numCols = 4
        val arr = arrayOf(
            "H 0", "1", "2", "3",
            "4", "5", "6", "7",
            "8", "9", "V 10", "11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            gearFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            GearMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            gearFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(2, 2)))
        )
        assertEquals(
            gearFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(2, 2)))
        )
        // Legal col
        assertEquals(
            gearFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board),
            GearMove(Axis.VERTICAL, Direction.FORWARD, 1, numRows, numCols)
        )
        assertEquals(
            gearFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board),
            GearMove(Axis.VERTICAL, Direction.FORWARD, 2, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            gearFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            gearFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 3, board),
            IllegalMove(lockCords = listOf(Pair(0, 4)))
        )
    }

    fun testMakeLightningArrowsMove() {
        val lightningFactory = MoveFactory(
            LightningMoveEffect(Axis.HORIZONTAL),
            LightningMoveEffect(Axis.VERTICAL),
            ArrowsValidator()
        )
        val numRows = 2
        val numCols = 3
        val arr = arrayOf(
            "B 0", "H 1", "2",
            "V 3", "4", "5"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            lightningFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            LightningMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            lightningFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(1, 0)))
        )
        // Legal col
        assertEquals(
            lightningFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board),
            LightningMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            lightningFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(0, 1)))
        )
    }

    fun testMakeWideArrowsMove() {
        val wideFactory = MoveFactory(
            WideMoveEffect(Axis.HORIZONTAL, 2),
            WideMoveEffect(Axis.VERTICAL, 2),
            ArrowsValidator()
        )
        val numRows = 3
        val numCols = 4
        val arr = arrayOf(
            "H 0", "1", "2", "3",
            "4", "5", "6", "7",
            "8", "9", "V 10", "11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            wideFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            WideMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols, 2)
        )
        // Illegal row
        assertEquals(
            wideFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(2, 2)))
        )
        assertEquals(
            wideFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(2, 2)))
        )
        // Legal col
        assertEquals(
            wideFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board),
            WideMove(Axis.VERTICAL, Direction.FORWARD, 1, numRows, numCols, 2)
        )
        assertEquals(
            wideFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board),
            WideMove(Axis.VERTICAL, Direction.FORWARD, 2, numRows, numCols, 2)
        )
        // Illegal col
        assertEquals(
            wideFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            wideFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 3, board),
            IllegalMove(lockCords = listOf(Pair(0, 4)))
        )
    }
}