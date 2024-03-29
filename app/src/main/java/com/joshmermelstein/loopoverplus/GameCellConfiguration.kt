package com.joshmermelstein.loopoverplus

// Gamecells need a lot of attributes to know how to draw themselves and how to behave in response
// to moves. This struct exists to encapsulate that in an attempt to pull some complexity our of
// GameCell.kt.
data class GameCellConfiguration(
    val isVert: Boolean = false,
    val isHoriz: Boolean = false,
    val hasBondUp: Boolean = false,
    val hasBondDown: Boolean = false,
    val hasBondLeft: Boolean = false,
    val hasBondRight: Boolean = false,
    val isLighting: Boolean = false,
    val isFixed: Boolean = false,
    val isEnabler: Boolean = false,
    val color: Int = 0,
    val numPips: Int = 0,
)

// Factory for making `GameCellConfiguration`s from cell IDs in levels
fun makeGamecellConfiguration(id: String): GameCellConfiguration {
    if (isNumeric(id)) {
        return GameCellConfiguration(
            color = id.toInt() % 6,
            numPips = (id.toInt() / 6) + 1
        )
    }

    val isVert = id.contains("V")
    val isHoriz = id.contains("H")
    val isLightning = id.contains("B")
    val hasBondUp = id.contains("U")
    val hasBondDown = id.contains("D")
    val hasBondLeft = id.contains("L")
    val hasBondRight = id.contains("R")
    val isFixed = id.contains("F")
    val isEnabler = id.contains("E")

    val parts = id.split(" ")
    val number = parts.last().toIntOrNull() ?: 0


    val numPips = when {
        isFixed -> number
        isEnabler -> 1
        else -> (number / 6) + 1
    }

    val color = when {
        isFixed -> 4
        isEnabler -> 5
        else -> number % 6
    }

    return GameCellConfiguration(
        isVert,
        isHoriz,
        hasBondUp,
        hasBondDown,
        hasBondLeft,
        hasBondRight,
        isLightning,
        isFixed,
        isEnabler,
        color,
        numPips
    )
}