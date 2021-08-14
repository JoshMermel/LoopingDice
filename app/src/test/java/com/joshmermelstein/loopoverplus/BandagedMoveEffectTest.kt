package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class BandagedMoveEffectTest : TestCase() {
    private val data = fakeGameCellMetadata()

    private val factory = MoveFactory(
        BandagedMoveEffect(Axis.HORIZONTAL),
        BandagedMoveEffect(Axis.VERTICAL),
        MoveValidator()
    )

    // Tests transitive bonds pushing each other
    fun testMakeMoveHorizontal() {
        val board = GameBoard(
            4,
            2,
            arrayOf("D 1", "2", "U 3", "D 4", "D 5", "U 6", "U 7", "8"),
            data
        )

        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 3, board)
        val expected = WideMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, 4, 2, 4)
        assertEquals(move, expected)
    }

    fun testMakeMoveVertical() {
        val board = GameBoard(
            2,
            4,
            arrayOf("R 1", "L 2", "R 3", "L 4", "5", "R 6", "L 7", "8"),
            data
        )

        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board)
        val expected = WideMove(Axis.VERTICAL, Direction.FORWARD, 0, 2, 4, 4)
        assertEquals(move, expected)
    }

    // transitively following bonds to see which rows/cols move would loop infinitely. Test that that doesn't happen.
    fun testMakeMoveHorizontalFullWraparound() {
        val board = GameBoard(
            3,
            2,
            arrayOf("U D 1", "2", "U D 3", "4", "U D 5", "6"),
            data
        )

        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 1, board)
        val expected = WideMove(Axis.HORIZONTAL, Direction.BACKWARD, 2, 3, 2, 3)
        assertEquals(move, expected)
    }

    fun testMakeMoveVerticalFullWraparound() {
        val board = GameBoard(
            2,
            3,
            arrayOf("R 1 L", "B 2 R L", "R 3 L", "4", "5", "6"),
            data
        )

        val move = factory.makeMove(Axis.VERTICAL, Direction.FORWARD, 1, board)
        val expected = WideMove(Axis.VERTICAL, Direction.FORWARD, 2, 2, 3, 3)
        assertEquals(move, expected)
    }
}