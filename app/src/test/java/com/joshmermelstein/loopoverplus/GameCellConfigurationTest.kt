package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class GameCellConfigurationTest : TestCase() {
    fun testBasics() {
        assertTrue(makeGamecellConfiguration("V").isVert)
        assertTrue(makeGamecellConfiguration("H").isHoriz)
        assertTrue(makeGamecellConfiguration("U").hasBondUp)
        assertTrue(makeGamecellConfiguration("D").hasBondDown)
        assertTrue(makeGamecellConfiguration("L").hasBondLeft)
        assertTrue(makeGamecellConfiguration("R").hasBondRight)
        assertTrue(makeGamecellConfiguration("E").isEnabler)
        assertTrue(makeGamecellConfiguration("B").isLighting)
        assertTrue(makeGamecellConfiguration("F").isFixed)
    }

    fun testColor() {
        // Normal specifying color
        assertEquals(makeGamecellConfiguration("V 0").color, 0)
        assertEquals(makeGamecellConfiguration("H 1").color, 1)
        assertEquals(makeGamecellConfiguration("U 2").color, 2)
        assertEquals(makeGamecellConfiguration("D 3").color, 3)
        assertEquals(makeGamecellConfiguration("L 0").color, 0)
        assertEquals(makeGamecellConfiguration("R 1").color, 1)
        assertEquals(makeGamecellConfiguration("B 2").color, 2)

        // Normal unspecified
        assertEquals(makeGamecellConfiguration("V").color, 0)
        assertEquals(makeGamecellConfiguration("H").color, 0)
        assertEquals(makeGamecellConfiguration("U").color, 0)

        // Enabler and Fixed are special
        assertEquals(makeGamecellConfiguration("E").color, 5)
        assertEquals(makeGamecellConfiguration("E 1").color, 5)
        assertEquals(makeGamecellConfiguration("F").color, 4)
        assertEquals(makeGamecellConfiguration("F 1").color, 4)
    }

    fun testPips() {
        // Normal
        for (i in 0..36) {
            assertEquals(makeGamecellConfiguration(i.toString()).numPips, 1 + (i / 6))
        }
        for (i in 0..6) {
            assertEquals(makeGamecellConfiguration("F $i").numPips, i)
        }
    }

    fun testHybrids() {
        // VH hybrids
        assertTrue(makeGamecellConfiguration("V E").isVert)
        assertTrue(makeGamecellConfiguration("H F").isHoriz)

        // Bond hybrids
        assertTrue(makeGamecellConfiguration("U E").hasBondUp)
        assertTrue(makeGamecellConfiguration("D L").hasBondDown)
        assertTrue(makeGamecellConfiguration("D L").hasBondLeft)

        // Enabler hybrids
        assertTrue(makeGamecellConfiguration("E V").isEnabler)
        assertTrue(makeGamecellConfiguration("E B").isEnabler)
        assertTrue(makeGamecellConfiguration("E F").isEnabler)

        // Bolt hybrids
        assertTrue(makeGamecellConfiguration("B E").isLighting)

        // Fixed hybrids
        assertTrue(makeGamecellConfiguration("F E").isFixed)
    }

    fun testMalformed() {
        // number in the wrong place
        assertEquals(makeGamecellConfiguration("1 F").numPips, 0)
        assertEquals(makeGamecellConfiguration("2 V").color, 0)
        assertEquals(makeGamecellConfiguration("3 H").color, 0)
    }

}