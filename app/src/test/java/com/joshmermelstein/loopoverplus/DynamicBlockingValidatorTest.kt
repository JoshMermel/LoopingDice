package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class DynamicBlockingValidatorTest : TestCase() {
    fun testMakeBasicDynamicMove() {
        val basicFactory = MoveFactory(
            BasicMoveEffect(Axis.HORIZONTAL),
            BasicMoveEffect(Axis.VERTICAL),
            DynamicBlockingValidator()
        )

        val numRows = 2
        val numCols = 3
        val arr = arrayOf(
            "F 0", "1", "2",
            "3", "4", "F 5"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            basicFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        )
        assertEquals(
            basicFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board),
            BasicMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            basicFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            basicFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(1, 2)))
        )
        // Legal col
        assertEquals(
            basicFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board),
            BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        )
        assertEquals(
            basicFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 2, board),
            BasicMove(Axis.VERTICAL, Direction.BACKWARD, 2, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            basicFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            basicFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(1, 2)))
        )
    }

    fun testMakeBandagedDynamicMove() {
        val bandagedFactory = MoveFactory(
            BandagedMoveEffect(Axis.HORIZONTAL),
            BandagedMoveEffect(Axis.VERTICAL),
            DynamicBlockingValidator()
        )

        val numRows = 3
        val numCols = 4
        val arr = arrayOf(
            "R ", "L 1", "2", "F 3",
            "4", "5", "D R 6", "D L 7",
            "F 8", "9", "U R 10", "U L 11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            bandagedFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            WideMove(Axis.HORIZONTAL, Direction.FORWARD, 1, numRows, numCols, 2)
        )
        // Illegal row
        assertEquals(
            bandagedFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(2, 0)))
        )
        // Legal col
        assertEquals(
            bandagedFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 1, board),
            WideMove(Axis.VERTICAL, Direction.BACKWARD, 0, numRows, numCols, 2)
        )
        assertEquals(
            bandagedFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board),
            WideMove(Axis.VERTICAL, Direction.FORWARD, 2, numRows, numCols, 2)
        )
        // Illegal col
        assertEquals(
            bandagedFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(0, 3)))
        )
    }

    // Carousel dynamic has a lot of edge cases. This one covers fixed cells in
    // top-left or bottom-right.
    fun testMakeCarouselDynamicMove1() {
        val carouselFactory = MoveFactory(
            CarouselMoveEffect(Axis.HORIZONTAL),
            CarouselMoveEffect(Axis.VERTICAL),
            DynamicBlockingValidator()
        )

        val numRows = 3
        val numCols = 4
        val arr1 = arrayOf(
            "F 0", "1", "2", "3",
            "4", "5", "6", "7",
            "8", "9", "10", "11"
        )
        val board1 = GameBoard(numRows, numCols, arr1, fakeGameCellMetadata())
        val arr2 = arrayOf(
            "0", "1", "2", "3",
            "4", "5", "6", "7",
            "8", "9", "10", "F 11"
        )
        val board2 = GameBoard(numRows, numCols, arr2, fakeGameCellMetadata())

        val expectedIllegals =
            listOf(
                IllegalMove(lockCords = listOf(Pair(3, 0))),
                IllegalMove(lockCords = listOf(Pair(2, 3))),
                IllegalMove(lockCords = listOf(Pair(0, 4))),
            )

        for (board in listOf(board1, board2)) {
            // Legal row
            assertEquals(
                carouselFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
                CarouselMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
            )
            assertEquals(
                carouselFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, board),
                CarouselMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, numRows, numCols)
            )
            // Illegal row
            assertTrue(
                carouselFactory.makeMove(
                    Axis.HORIZONTAL,
                    Direction.FORWARD,
                    2,
                    board
                ) in expectedIllegals
            )
            // Legal col
            assertEquals(
                carouselFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board),
                CarouselMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
            )
            assertEquals(
                carouselFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 3, board),
                CarouselMove(Axis.VERTICAL, Direction.BACKWARD, 3, numRows, numCols)
            )
            // Illegal col
            assertTrue(
                carouselFactory.makeMove(
                    Axis.VERTICAL,
                    Direction.FORWARD,
                    3,
                    board
                ) in expectedIllegals
            )
        }
    }

    // Carousel dynamic has a lot of edge cases. This one covers fixed cells in
    // top-left or bottom-right.
    fun testMakeCarouselDynamicMove2() {
        val carouselFactory = MoveFactory(
            CarouselMoveEffect(Axis.HORIZONTAL),
            CarouselMoveEffect(Axis.VERTICAL),
            DynamicBlockingValidator()
        )

        val numRows = 3
        val numCols = 4
        val arr1 = arrayOf(
            "0", "1", "2", "F 3",
            "4", "5", "6", "7",
            "8", "9", "10", "11"
        )
        val board1 = GameBoard(numRows, numCols, arr1, fakeGameCellMetadata())
        val arr2 = arrayOf(
            "0", "1", "2", "3",
            "4", "5", "6", "7",
            "F 8", "9", "10", "11"
        )
        val board2 = GameBoard(numRows, numCols, arr2, fakeGameCellMetadata())

        val expectedIllegals =
            listOf(
                IllegalMove(lockCords = listOf(Pair(2, 0))),
                IllegalMove(lockCords = listOf(Pair(2, 4))),
                IllegalMove(lockCords = listOf(Pair(0, 3))),
                IllegalMove(lockCords = listOf(Pair(3, 3))),
            )

        for (board in listOf(board1, board2)) {
            // Legal row
            assertEquals(
                carouselFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 2, board),
                CarouselMove(Axis.HORIZONTAL, Direction.FORWARD, 2, numRows, numCols)
            )
            // Illegal row
            assertTrue(
                carouselFactory.makeMove(
                    Axis.HORIZONTAL,
                    Direction.BACKWARD,
                    2,
                    board
                ) in expectedIllegals
            )
            // Legal col
            assertEquals(
                carouselFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 3, board),
                CarouselMove(Axis.VERTICAL, Direction.FORWARD, 3, numRows, numCols)
            )
            // Illegal col
            assertTrue(
                carouselFactory.makeMove(
                    Axis.VERTICAL,
                    Direction.BACKWARD,
                    3,
                    board
                ) in expectedIllegals
            )
        }
    }

    fun testMakeGearDynamicMove() {
        val gearFactory = MoveFactory(
            GearMoveEffect(Axis.HORIZONTAL),
            GearMoveEffect(Axis.VERTICAL),
            DynamicBlockingValidator()
        )

        val numRows = 3
        val numCols = 4
        val arr = arrayOf(
            "F 0", "1", "2", "3",
            "4", "5", "6", "7",
            "8", "9", "F 10", "11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            gearFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            GearMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            gearFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board),
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
        assertEquals(
            gearFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 3, board),
            GearMove(Axis.VERTICAL, Direction.BACKWARD, 3, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            gearFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        assertEquals(
            gearFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(2, 2)))
        )
    }

    fun testMakeLightningDynamicMove() {
        val lightningFactory = MoveFactory(
            LightningMoveEffect(Axis.HORIZONTAL),
            LightningMoveEffect(Axis.VERTICAL),
            DynamicBlockingValidator()
        )

        val numRows = 4
        val numCols = 5
        val arr = arrayOf(
            "0", "F 1", "2", "3", "4",
            "F 5", "B 6", "7", "8", "F 9",
            "10", "11", "12", "B F 13", "14",
            "15", "F 16", "17", "18", "19",
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            lightningFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            BasicMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols)
        )
        assertEquals(
            lightningFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, board),
            LightningMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, numRows, numCols)
        )
        // Illegal row
        assertEquals(
            lightningFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(1, 4)))
        )
        assertEquals(
            lightningFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(1, 0)))
        )
        assertEquals(
            lightningFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 2, board),
            IllegalMove(lockCords = listOf(Pair(2, 3)))
        )
        // Legal col
        assertEquals(
            lightningFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 0, board),
            BasicMove(Axis.VERTICAL, Direction.FORWARD, 0, numRows, numCols)
        )
        assertEquals(
            lightningFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 3, board),
            LightningMove(Axis.VERTICAL, Direction.BACKWARD, 3, numRows, numCols)
        )
        // Illegal col
        assertEquals(
            lightningFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(3, 1)))
        )
        assertEquals(
            lightningFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 1, board),
            IllegalMove(lockCords = listOf(Pair(0, 1)))
        )
        assertEquals(
            lightningFactory.makeMove(Axis.VERTICAL, Direction.FORWARD, 3, board),
            IllegalMove(lockCords = listOf(Pair(2, 3)))
        )
    }

    fun testMakeWideDynamicMove() {
        val wideFactory = MoveFactory(
            WideMoveEffect(Axis.HORIZONTAL, 2),
            WideMoveEffect(Axis.VERTICAL, 2),
            DynamicBlockingValidator()
        )

        val numRows = 3
        val numCols = 4
        val arr = arrayOf(
            "F 0", "1", "2", "3",
            "4", "5", "6", "7",
            "8", "9", "F 10", "11"
        )
        val board = GameBoard(numRows, numCols, arr, fakeGameCellMetadata())

        // Legal row
        assertEquals(
            wideFactory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, 0, board),
            WideMove(Axis.HORIZONTAL, Direction.FORWARD, 0, numRows, numCols, 2)
        )
        // Illegal row
        assertEquals(
            wideFactory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
        // Legal col
        assertEquals(
            wideFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 1, board),
            WideMove(Axis.VERTICAL, Direction.BACKWARD, 1, numRows, numCols, 2)
        )
        // Illegal col
        assertEquals(
            wideFactory.makeMove(Axis.VERTICAL, Direction.BACKWARD, 0, board),
            IllegalMove(lockCords = listOf(Pair(0, 0)))
        )
    }
}