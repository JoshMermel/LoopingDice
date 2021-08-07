package com.joshmermelstein.loopoverplus

// TODO(jmerm): tests for the parsing in this file.

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

// TODO(jmerm): validate that the combination is sensible (and then return what?)
// Factory for making `GameCellConfiguration`s from cell IDs in levels
fun makeGamecellConfiguration(id: String): GameCellConfiguration {
    if (isNumeric(id)) {
        return GameCellConfiguration(
            color = id.toInt() % 6,
            numPips = (id.toInt() / 6) + 1
        )
    }

    // TODO(jmerm): change Lightning to stop conflicting with bondLeft
    // TODO(jmerm): make this whole situation more consistent and rewrite all levels :-/
    val isVert = id.contains("V")
    val isHoriz = id.contains("H")
    val isLightning = id.startsWith("L")
    val hasBondUp = id.startsWith("B") && id.contains("U")
    val hasBondDown = id.startsWith("B") && id.contains("D")
    val hasBondLeft = id.startsWith("B") && id.contains("L")
    val hasBondRight = id.startsWith("B") && id.contains("R")
    val isFixed = id.contains("F")
    val isEnabler = id.contains("E")

    val parts = id.split(" ")
    val number = parts.getOrNull(1)?.toIntOrNull() ?: 0

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