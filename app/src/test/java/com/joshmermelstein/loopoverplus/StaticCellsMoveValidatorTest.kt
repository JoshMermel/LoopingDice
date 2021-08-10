package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class StaticCellsMoveValidatorTest : TestCase() {
    fun testMakeBasicStaticMove() {
        val basicFactory = MoveFactory(
            BasicMoveEffect(Axis.HORIZONTAL),
            BasicMoveEffect(Axis.VERTICAL),
            StaticCellsValidator()
        )

        val numRows = 2
        val numCols = 3
        val arr = arrayOf(
            "F 0", "1", "2",
            "3", "4", "5"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            basicFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 1, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            basicFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
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

    fun testMakeBandagedStaticMove() {
        val bandagedFactory = MoveFactory(
            BandagedMoveEffect(Axis.HORIZONTAL),
            BandagedMoveEffect(Axis.VERTICAL),
            StaticCellsValidator()
        )

        val numRows = 3
        val numCols = 4
        val arr = arrayOf(
            "B 0 R", "B 1 L", "2", "F 3",
            "4", "5", "B 6 D R", "B 7 D L",
            "8", "9", "B 10 U R", "B 11 U L"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            bandagedFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            WideMove(Axis.HORIZONTAL, Direction.FORWARD, 1, numRows, numCols, 2)
        )
        // Illegal row
        assertEquals(
            bandagedFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 3)))
        )
        // Legal col
        assertEquals(
            bandagedFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board),
            WideMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols, 2)
        )
        // Illegal col
        assertEquals(
            bandagedFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 3, board),
            IllegalMove(lockCords = listOf(Pair(0, 3)))
        )
    }

    fun testMakeCarouselStaticMove() {
        val carouselFactory = MoveFactory(
            CarouselMoveEffect(Axis.HORIZONTAL),
            CarouselMoveEffect(Axis.VERTICAL),
            StaticCellsValidator()
        )

        val numRows = 3
        val numCols = 4
        val arr = arrayOf(
            "F 0", "1", "2", "3",
            "4", "5", "6", "7",
            "8", "9", "10", "11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            carouselFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board),
            CarouselMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            carouselFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            carouselFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(3, 0)))
        )
        // Legal col
        assertEquals(
            carouselFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board),
            CarouselMove(Axis.VERTICAL, Direction.FORWARD, 2, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            carouselFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            carouselFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 3, board),
            IllegalMove(lockCords = listOf(Pair(0, 4)))
        )
    }

    fun testMakeGearStaticMove() {
        val gearFactory = MoveFactory(
            GearMoveEffect(Axis.HORIZONTAL),
            GearMoveEffect(Axis.VERTICAL),
            StaticCellsValidator()
        )

        val numRows = 3
        val numCols = 4
        val arr = arrayOf(
            "F 0", "1", "2", "3",
            "4", "5", "6", "7",
            "8", "9", "10", "11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            gearFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            GearMove(Axis.HORIZONTAL, Direction.FORWARD, 1, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            gearFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            gearFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(3, 0)))
        )
        // Legal col
        assertEquals(
            gearFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board),
            GearMove(Axis.VERTICAL, Direction.FORWARD, 1, numRows, numCols)
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

    fun testMakeLightningStaticMove() {
        val lightningFactory = MoveFactory(
            LightningMoveEffect(Axis.HORIZONTAL),
            LightningMoveEffect(Axis.VERTICAL),
            StaticCellsValidator()
        )

        val numRows = 2
        val numCols = 3
        val arr = arrayOf(
            "B 0", "1", "F 2",
            "3", "4", "B 5"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            lightningFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            LightningMove(Axis.HORIZONTAL, Direction.FORWARD, 1, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            lightningFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 2)))
        )
        // Legal col
        assertEquals(
            lightningFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board),
            LightningMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            lightningFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(0, 2)))
        )
    }

    fun testMakeWideStaticMove() {
        val wideFactory = MoveFactory(
            WideMoveEffect(Axis.HORIZONTAL, 2),
            WideMoveEffect(Axis.VERTICAL, 2),
            StaticCellsValidator()
        )

        val numRows = 3
        val numCols = 4
        val arr = arrayOf(
            "F 0", "1", "2", "3",
            "4", "5", "6", "7",
            "8", "9", "10", "11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            wideFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            WideMove(Axis.HORIZONTAL, Direction.FORWARD, 1, numRows, numCols, 2)
        )
        // Illegal row
        assertEquals(
            wideFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            wideFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(3, 0)))
        )
        // Legal col
        assertEquals(
            wideFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board),
            WideMove(Axis.VERTICAL, Direction.FORWARD, 1, numRows, numCols, 2)
        )
        assertEquals(
            wideFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board),
            WideMove(Axis.VERTICAL, Direction.FORWARD, 1, numRows, numCols, 2)
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