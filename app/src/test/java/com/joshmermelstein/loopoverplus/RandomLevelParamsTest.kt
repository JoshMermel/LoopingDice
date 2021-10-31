package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

class RandomLevelParamsTest : TestCase() {

    fun testToAndFromString() {
        repeat (10000) {
            val params = feelingLucky()
            assertEquals(params, randomLevelParamsFromString(params.toString()))
        }
    }
}