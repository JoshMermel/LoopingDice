package com.joshmermelstein.loopoverplus

import junit.framework.TestCase

// Tests for various utilities around random level generation.

class RandomLevelTest : TestCase() {
    // In dynamic bandaging mode, bicolor boards should be a mix of one-pip fixed cells and one
    // color of normal cell
    fun testBicolorDynamic() {
        for (numRows in (2..6)) {
            for (numCols in (2..5)) {
                for (numBandaged in arrayOf("Rare", "Common", "Frequent")) {
                    val board =
                        generateDynamicBandagingGoal(numRows, numCols, "Bicolor", numBandaged)
                    val keys = board.groupingBy { it }.eachCount().keys

                    assertEquals(2, keys.size)
                    assertTrue(keys.contains("F 1"))
                }
            }
        }
    }

    // Other than dynamic mode, bicolor levels should be half of one color and half of another.
    fun testBicolorNonDynamic() {
        for (numRows in (2..6)) {
            for (numCols in (2..6)) {
                val board = generateBasicGoal(numRows, numCols, "Bicolor")
                val keys = board.groupingBy { it }.eachCount().keys

                assertEquals(2, keys.size)
            }
        }
    }

    // In some modes, black squares have special meanings and are replaced by gold cells in
    // 5-column boards
    fun testColumnsNoBlackSquares() {
        val numCols = 5
        for (numRows in (2..6)) {
            for (frequency in arrayOf("Rare", "Common", "Frequent")) {
                for (board in arrayOf(
                    generateDynamicBandagingGoal(
                        numRows,
                        numCols,
                        "Columns",
                        frequency
                    ),
                    generateStaticCellGoal(numRows, numCols, "Columns"),
                    generateBandagedGoal(numRows, numCols, "Columns", frequency)
                )) {
                    val keys = board.groupingBy { it }.eachCount().keys
                    assertFalse(keys.contains("4"))
                }
            }
        }
    }

    // Basic columns boards should have every cell be the same as the one above/below it.
    fun testColumnsBasic() {
        for (numCols in (2..6)) {
            val board = generateBasicGoal(3, numCols, "Columns")
            for (offset in (0 until 3 * numCols)) {
                assertEquals(board[offset], board[offset % numCols])
            }
        }
    }

    // Basic unique levels should have every cell be unique
    fun testUniqueBasic() {
        for (numRows in (2..6)) {
            for (numCols in (2..6)) {
                val board = generateBasicGoal(numRows, numCols, "Unique")
                val frequencies = board.groupingBy { it }.eachCount().values.toSet()
                assertEquals(frequencies.size, 1)
            }
        }
    }

    fun testUniqueBandaged() {
        for (numRows in (2..6)) {
            for (numCols in (2..5)) {
                for (numBandaged in arrayOf("Rare", "Common", "Frequent")) {
                    val board =
                        generateBandagedGoal(numRows, numCols, "Unique", numBandaged)

                    // Other than bandaged cells, all cells must be unique in Bandaged mode.
                    val unbandagedFrequencies =
                        board.filter { !it.startsWith("B") }.groupingBy { it }
                            .eachCount().values.toSet()
                    assertTrue(unbandagedFrequencies.size <= 1)

                    // Bandaged cells should have IDs <= 5 so they compare with each other correctly
                    val bandagedIds =
                        board.filter { it.startsWith("B") }.map { it.split(" ")[1].toInt() }
                    bandagedIds.forEach { assertTrue(it <= 5) }
                }
            }
        }
    }

    // other than fixed cells, all cells must be unique in dynamic bandaging mode.
    fun testUniqueDynamic() {
        for (numRows in (2..6)) {
            for (numCols in (2..5)) {
                for (numBandaged in arrayOf("Rare", "Common", "Frequent")) {
                    val board =
                        generateDynamicBandagingGoal(numRows, numCols, "Unique", numBandaged)
                    val frequencies = board.filter { !it.startsWith("F") }.groupingBy { it }
                        .eachCount().values.toSet()
                    assertEquals(frequencies.size, 1)
                }
            }
        }
    }

    // In bandaged + Speckled, all bandaged should be the same color
    fun testBandagedSpeckled() {
        for (numRows in (2..6)) {
            for (numCols in (2..5)) {
                for (numBandaged in arrayOf("Rare", "Common", "Frequent")) {
                    val board =
                        generateBandagedGoal(numRows, numCols, "Speckled", numBandaged)
                    val keys = board.filter { it.startsWith("B") }.map { it[3] }.groupingBy { it }
                        .eachCount().keys.toSet()
                    assertEquals(keys.size, 1)
                }
            }
        }
    }

    // In bandaged + Speckled, all non-dynamic should be the same color
    fun testDynamicSpeckled() {
        for (numRows in (2..6)) {
            for (numCols in (2..5)) {
                for (numBandaged in arrayOf("Rare", "Common", "Frequent")) {
                    val board =
                        generateDynamicBandagingGoal(numRows, numCols, "Speckled", numBandaged)
                    val keys = board.filter { !it.startsWith("F") }.map { it }.groupingBy { it }
                        .eachCount().keys.toSet()
                    assertEquals(keys.size, 1)
                }
            }
        }
    }

    fun testArrowsUnique() {
        for (numRows in (2..6)) {
            for (numCols in (2..5)) {
                for (numArrows in arrayOf("Rare", "Common", "Frequent")) {
                    val board =
                        generateArrowsGoal(numRows, numCols, "Unique", numArrows)
                    // Arrows cells should have IDs <= 5 so they compare with each other correctly
                    val lightningIds =
                        board.filter { it.startsWith("H") || it.startsWith("V") }.map { it.split(" ")[1].toInt() }
                    lightningIds.forEach { assertTrue(it <= 5) }
                }
            }
        }
    }

    fun testLightningUnique() {
        for (numRows in (2..6)) {
            for (numCols in (2..5)) {
                for (numBolts in arrayOf("Rare", "Common", "Frequent")) {
                    val board =
                        generateLightningGoal(numRows, numCols, "Unique", numBolts)
                    // Lightning cells should have IDs <= 5 so they compare with each other correctly
                    val lightningIds =
                        board.filter { it.startsWith("L") }.map { it.split(" ")[1].toInt() }
                    lightningIds.forEach { assertTrue(it <= 5) }
                }
            }
        }
    }

}