package com.joshmermelstein.loopoverplus

import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase

class BandagedMoveFactoryTest : TestCase() {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    private val factory = BandagedMoveFactory()

    // Tests transitive bonds pushing each other
    fun testMakeMoveHorizontal() {
        val board = GameBoard(4,2,arrayOf("B 1 D", "2", "B 3 U", "B 4 D", "B 5 D", "B 6 U", "B 7 U", "8"), appContext)

        val move = factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, 3, board)
        val expected = WideMove(Axis.HORIZONTAL, Direction.BACKWARD, 0, 4, 2, 4)
        assertEquals(move, expected)
    }

    fun testMakeMoveVertical() {
        // TODO(jmerm)
    }

    // TODO(jmerm): wraparound tests to make sure we don't infinite loops

    fun testMakeHighlightsHorizontal() {
        // TODO(jmerm)
    }

    fun testMakeHighlightsVertical() {
        // TODO(jmerm)
    }
}