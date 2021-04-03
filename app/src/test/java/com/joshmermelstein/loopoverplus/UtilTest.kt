package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class UtilTest : TestCase() {

    fun testBoundsWidth() {
        val bounds = Bounds(0.0,0.0,50.0,100.0)
        assertEquals(bounds.width(), 50.0)
    }

    fun testBoundsHeight() {
        val bounds = Bounds(0.0,0.0,50.0,100.0)
        assertEquals(bounds.height(), 100.0)
    }

    fun testMod() {
        assertEquals(mod(5, 3), 2)
        assertEquals(mod(-5, 3), 1)
    }

    fun testOpposite() {
        assertEquals(opposite(Direction.BACKWARD), Direction.FORWARD)
        assertEquals(opposite(Direction.FORWARD), Direction.BACKWARD)
    }
}