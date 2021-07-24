package com.joshmermelstein.loopoverplus

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class LevelMetadata(
    var next: String?,
    var displayId: String,
    var canonicalId: String,
    val fourStar: Int,
    val threeStar: Int,
    val twoStar: Int
)

// A singleton for holding metadata about levels
// |packData| for which levels are in packs
// |levelData| for par and which levels come after each other
class MetadataSingleton private constructor(private val context: Context) {
    private val levelData: MutableMap<String, LevelMetadata> = mutableMapOf()
    val packData: MutableMap<String, List<String>> = mutableMapOf()

    init {
        var prevId: String? = null
        for (filename in arrayOf(
            "intro",
            "wide_0",
            "dynamic_0",
            "bandaged_0",
            "carousel_0",
            "arrows_0",
            "lightning_0",
            "gear_0",
            "enabler_0",
            "static_0",
            "hybrid_wc_0",
            "hybrid_wg_0",
            "wide_1",
            "dynamic_1",
            "bandaged_1",
            "carousel_1",
            "arrows_1",
            "lightning_1",
            "gear_1",
            "enabler_1",
            "static_1",
            "hybrid_wc_1",
            "hybrid_wg_1",
            "wide_2",
            "dynamic_2",
            "bandaged_2",
            "carousel_2",
            "arrows_2",
            "lightning_2",
            "gear_2",
            "enabler_2",
            "static_2",
            "hybrid_wc_2",
            "hybrid_wg_2",
        )) {
            val reader =
                BufferedReader(InputStreamReader(context.assets.open("packs/$filename.txt")))
            val title: String = reader.readLine()!!
            val pack: MutableList<String> = mutableListOf()

            var line: String? = reader.readLine()
            while (line != null) {
                val level = parseLevel(line)
                if (level != null) {
                    pack.add(level.canonicalId)
                    levelData[level.canonicalId] = level
                    levelData[prevId]?.next = level.canonicalId
                    prevId = level.canonicalId
                }
                line = reader.readLine()
            }
            packData[title] = pack
            prevId = null
        }
    }

    private fun parseLevel(line: String): LevelMetadata? {
        val parts = line.split(" ")
        if (parts.size < 5) {
            Log.e("LoopingDice", "Failed to load level from \"$line\", not enough parts")
            return null
        }

        val displayId = parts[0]
        val canonicalId = parts[1]
        val fourStar = parts[2]
        val threeStar = parts[3]
        val twoStar = parts[4]

        if (!isNumeric(fourStar) || !isNumeric(threeStar) || !isNumeric(twoStar)) {
            Log.e("LoopingDice", "Failed to load level from \"$line\", par must be an int")
            return null
        }

        return LevelMetadata(
            null,
            displayId,
            canonicalId,
            fourStar.toInt(),
            threeStar.toInt(),
            twoStar.toInt()
        )
    }

    // Gets data about a level from its canonical ID.
    // For Infinity (randomly generated) levels, this creates a sensible response from thin air.
    fun getLevelData(id: String): LevelMetadata? {
        if (!id.startsWith("∞")) {
            return levelData[id]
        }
        return LevelMetadata(
            null,
            "∞",
            id,
            1000000,
            1000000,
            1000000
        )
    }

    fun getNumComplete(packId: String): String {
        val highscores: SharedPreferences =
            context.getSharedPreferences("highscores", Context.MODE_PRIVATE)
        val levelIds = packData[packId] ?: return "0 / 0"

        val numComplete = levelIds.map {
            if (highscores.contains(it)) 1 else 0
        }.sum()

        return "$numComplete / ${levelIds.size}"
    }

    companion object : SingletonHolder<MetadataSingleton, Context>(::MetadataSingleton)
}