package com.joshmermelstein.loopoverplus


data class GameCellConfiguration(
    val isVert: Boolean = false,
    val isHoriz: Boolean = false,
    val hasBondUp: Boolean = false,
    val hasBondDown: Boolean = false,
    val hasBondRight: Boolean = false,
    val hasBondLeft: Boolean = false,
    val isLighting: Boolean = false,
    val isFixed: Boolean = false,
    val isEnabler: Boolean = false,
    val color: Int = 0,
    val pips: Int = 0,
)

// TODO(jmerm): support hybrids
// TODO(jmerm): validate that the combination is sensible
fun makeGamecellConfiguration(id: String): GameCellConfiguration {
    val parts = id.split(" ")

    return when {
        id == "E" -> GameCellConfiguration(isEnabler = true, color = 5, pips = 1)
        id.startsWith("F") -> GameCellConfiguration(
            isFixed = true,
            color = 4,
            pips = parts[1].toInt()
        )
        id.startsWith("B") -> GameCellConfiguration(
            hasBondUp = id.contains("U"),
            hasBondDown = id.contains("D"),
            hasBondRight = id.contains("R"),
            hasBondLeft = id.contains("L"),
            color = parts[1].toInt() % 6,
            pips = (parts[1].toInt() / 6) + 1,
        )
        id.startsWith("H") -> GameCellConfiguration(
            isHoriz = true,
            color = parts[1].toInt() % 6,
            pips = 1
        )
        id.startsWith("V") -> GameCellConfiguration(
            isVert = true,
            color = parts[1].toInt() % 6,
            pips = 1
        )
        id.startsWith("L") -> GameCellConfiguration(
            isLighting = true,
            color = parts[1].toInt() % 6,
            pips = 1
        )
        else -> GameCellConfiguration(
            color = parts[0].toInt() % 6,
            pips = (parts[0].toInt() / 6) + 1,
        )
    }
}