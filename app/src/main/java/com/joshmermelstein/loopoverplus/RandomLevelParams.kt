package com.joshmermelstein.loopoverplus

import android.content.Context
import android.content.SharedPreferences
import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize

/*
 * This file defines the RandomLevelParams struct and a few related types and helpers.
 * A RandomLevelParams contains enough info to generate a random level.
 */

@Parcelize
data class RandomLevelParams(
    val numRows: Int,
    val numCols: Int,
    val colorScheme: ColorScheme,
    val rowMode: Mode,
    val colMode: Mode?,
    val rowDepth: Int?,
    val colDepth: Int?,
    val density: Density?,
    val blockedRows: Int?,
    val blockedCols: Int?,
) : Parcelable {
    override fun toString(): String {
        return "$numRows,$numCols,$colorScheme,$rowMode,$colMode,$rowDepth,$colDepth,$density,$blockedRows,$blockedCols"
    }

    private fun sizeToString(): String {
        return "Size: $numRows x $numCols"
    }

    private fun colorSchemeToString(): String {
        return "\nColor Scheme: ${colorScheme.toString().lowercase()}"

    }

    private fun densityToString(): String {
        return density.toString().lowercase()
    }

    // Doesn't handle static because that's handled elsewhere
    private fun modeToString(m: Mode, depth: Int?): String {
        return when (m) {
            Mode.WIDE -> "Wide(" + depth?.toString() + ")"
            Mode.BANDAGED -> "Bandaged (${densityToString()})"
            Mode.ENABLER -> "Enabler (${densityToString()})"
            Mode.ARROWS -> "Arrows (${densityToString()})"
            Mode.LIGHTNING -> "Lightning (${densityToString()})"
            Mode.DYNAMIC -> "Dynamic Blocking (${densityToString()})"
            Mode.GEAR -> "Gear"
            Mode.CAROUSEL -> "Carousel"
            Mode.STATIC -> "Static" // should never happen
        }
    }

    private fun modesToString(): String {
        return "\n" + when {
            rowMode == Mode.STATIC -> "Mode: Static Cells ($rowDepth x $colDepth)"
            colMode == null -> "Mode: ${modeToString(rowMode, rowDepth)}"
            else -> "Modes: ${modeToString(rowMode, rowDepth)} x ${modeToString(colMode, colDepth)}"
        }
    }

    private fun blockedToString(): String {
        return if (blockedRows == null || blockedCols == null) {
            ""
        } else {
            "\nBlocked: $blockedRows x $blockedCols"
        }
    }

    fun toUserString(): String {
        return sizeToString() + colorSchemeToString() + modesToString() + blockedToString()
    }
}

enum class Density(val userString: Int) {
    RARE(R.string.infinityDensityRare),
    COMMON(R.string.infinityDensityCommon),
    FREQUENT(R.string.infinityDensityFrequent);
}

enum class ColorScheme(val userString: Int) {
    BICOLOR(R.string.infinityColorSchemeBicolor) {
        override fun maxNumRows() = 8
        override fun maxNumCols(m : Mode) = 8
    },
    SPECKLED(R.string.infinityColorSchemeSpeckled) {
        override fun maxNumRows() = 8
        override fun maxNumCols(m : Mode) = 8
    },
    COLUMNS(R.string.infinityColorSchemeColumns) {
        override fun maxNumRows() = 8
        override fun maxNumCols(m : Mode) = if (m.hasSpecialColor()) 5 else 6
    },
    UNIQUE(R.string.infinityColorSchemeUnique) {
        override fun maxNumRows() = 6
        override fun maxNumCols(m : Mode) = if (m.hasSpecialColor()) 5 else 6
    };

    abstract fun maxNumRows(): Int
    abstract fun maxNumCols(m : Mode): Int
}

enum class Mode(val userString: Int) {
    WIDE(R.string.infinityModeWide) {
        override fun hasSpecialColor(): Boolean = false
        override fun hasDensity(): Boolean = false
    },
    CAROUSEL(R.string.infinityModeCarousel) {
        override fun hasSpecialColor(): Boolean = false
        override fun hasDensity(): Boolean = false
    },
    GEAR(R.string.infinityModeGear) {
        override fun hasSpecialColor(): Boolean = false
        override fun hasDensity(): Boolean = false
    },
    DYNAMIC(R.string.infinityModeDynamic) {
        override fun hasSpecialColor(): Boolean = true
        override fun hasDensity(): Boolean = true
    },
    BANDAGED(R.string.infinityModeBandaged) {
        override fun hasSpecialColor(): Boolean = true
        override fun hasDensity(): Boolean = true
    },
    LIGHTNING(R.string.infinityModeLightning) {
        override fun hasSpecialColor(): Boolean = false
        override fun hasDensity(): Boolean = true
    },
    ARROWS(R.string.infinityModeArrows) {
        override fun hasSpecialColor(): Boolean = false
        override fun hasDensity(): Boolean = true
    },
    ENABLER(R.string.infinityModeEnabler) {
        override fun hasSpecialColor(): Boolean = true
        override fun hasDensity(): Boolean = true
    },
    STATIC(R.string.infinityModeStatic) {
        override fun hasSpecialColor(): Boolean = true
        override fun hasDensity(): Boolean = false
    };

    abstract fun hasSpecialColor(): Boolean
    abstract fun hasDensity(): Boolean
}

// Generates a random, valid RandomLevelParams.
fun feelingLucky(): RandomLevelParams {
    val rowMode = Mode.values().random()

    val colMode = if (rowMode in arrayOf(Mode.WIDE, Mode.CAROUSEL, Mode.GEAR)) {
        // We include rowMode a few extra times to make rowMode=colMode more likely than otherwise.
        arrayOf(Mode.WIDE, Mode.CAROUSEL, Mode.GEAR, rowMode, rowMode).random()
    } else {
        null
    }

    val colorScheme = ColorScheme.values().random()

    val numRows = (2..colorScheme.maxNumRows()).random()
    val numCols = (2..colorScheme.maxNumCols(rowMode)).random()

    // Probability distribution hack to make small depth more likely than large depth.
    val rowDepth = when (rowMode) {
        Mode.WIDE -> minOf((1..numRows).random(), (1..numRows).random())
        Mode.STATIC -> minOf((1 until numRows).random(), (1 until numRows).random())
        else -> null
    }
    val colDepth = when {
        colMode == Mode.WIDE -> minOf((1..numCols).random(), (1..numCols).random())
        rowMode == Mode.STATIC -> minOf((1 until numCols).random(), (1 until numCols).random())
        else -> null
    }

    val density = if (rowMode.hasDensity()) {
        Density.values().random()
    } else {
        null
    }

    val numBlockedRows = if (rowMode == Mode.STATIC) {
        (1..(numRows - rowDepth!!)).random()
    } else {
        null
    }

    val numBlockedCols = if (rowMode == Mode.STATIC) {
        (1..(numCols - colDepth!!)).random()
    } else {
        null
    }

    return RandomLevelParams(
        numRows,
        numCols,
        colorScheme,
        rowMode,
        colMode,
        rowDepth,
        colDepth,
        density,
        numBlockedRows,
        numBlockedCols
    )
}

fun randomLevelParamsFromString(s: String): RandomLevelParams {
    Log.d("jmerm", "parsing $s")
    val parts = s.split(",")
    val numRows = parts[0].toInt()
    val numCols = parts[1].toInt()
    val colorScheme = when (parts[2]) {
        "BICOLOR" -> ColorScheme.BICOLOR
        "SPECKLED" -> ColorScheme.SPECKLED
        "COLUMNS" -> ColorScheme.COLUMNS
        "UNIQUE" -> ColorScheme.UNIQUE
        else -> ColorScheme.BICOLOR
    }
    val rowMode = when (parts[3]) {
        "WIDE" -> Mode.WIDE
        "CAROUSEL" -> Mode.CAROUSEL
        "GEAR" -> Mode.GEAR
        "DYNAMIC" -> Mode.DYNAMIC
        "BANDAGED" -> Mode.BANDAGED
        "LIGHTNING" -> Mode.LIGHTNING
        "ARROWS" -> Mode.ARROWS
        "ENABLER" -> Mode.ENABLER
        "STATIC" -> Mode.STATIC
        else -> Mode.WIDE
    }
    val colMode = when (parts[4]) {
        "WIDE" -> Mode.WIDE
        "CAROUSEL" -> Mode.CAROUSEL
        "GEAR" -> Mode.GEAR
        else -> null
    }
    val rowDepth = parts[5].toIntOrNull()
    val colDepth = parts[6].toIntOrNull()
    val density = when (parts[7]) {
        "RARE" -> Density.RARE
        "COMMON" -> Density.COMMON
        "FREQUENT" -> Density.FREQUENT
        else -> null
    }
    val blockedRows = parts[8].toIntOrNull()
    val blockedCols = parts[9].toIntOrNull()

    return RandomLevelParams(
        numRows,
        numCols,
        colorScheme,
        rowMode,
        colMode,
        rowDepth,
        colDepth,
        density,
        blockedRows,
        blockedCols
    )
}

fun saveParamsToRecentLevels(recentLevels: SharedPreferences, params: RandomLevelParams) {
    val old = recentLevels.getString("LastTwenty", "") ?: ""
    val parts: MutableList<String> = old.split("\n").toMutableList()
    parts.remove("$params")
    parts.add("$params")
    with(recentLevels.edit()) {
        putString("LastTwenty", parts.takeLast(20).joinToString("\n"))
        apply()
    }
}

fun getRecentLevels(context: Context): List<RandomLevelParams>? {
    return context.getSharedPreferences("RecentLevels", Context.MODE_PRIVATE)
        .getString("LastTwenty", null)
        ?.split("\n")?.filter { it.isNotEmpty() }?.map { randomLevelParamsFromString(it) }
        ?.reversed()
}