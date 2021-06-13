package com.joshmermelstein.loopoverplus

import android.content.Context
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

class PackMetadata(
    val title: String,
    val levels: MutableList<String>
)

// A singleton for holding metadata about levels
// |packData| for which levels are in packs
// |levelData| for par and which levels come after each other
class MetadataSingleton private constructor(context: Context) {
    private val levelData: MutableMap<String, LevelMetadata> = mutableMapOf()
    val packData: MutableList<PackMetadata> = mutableListOf()

    init {
        var prevId: String? = null
        for (filename in arrayOf(
            "intro",
            "sampler",
            "wide",
            "dynamic",
            "carousel",
            "bandaged",
            "gear",
            "axis_locked",
            "enabler",
            "static",
            "hybrid_wc",
            "hybrid_wg",
        )) {
            val reader =
                BufferedReader(InputStreamReader(context.assets.open("packs/$filename.txt")))
            val title: String = reader.readLine()!!
            val pack = PackMetadata(title, mutableListOf())

            var line: String? = reader.readLine()
            while (line != null) {
                val level = parseLevel(line)
                if (level != null) {
                    pack.levels.add(level.canonicalId)
                    levelData[level.canonicalId] = level
                    levelData[prevId]?.next = level.canonicalId
                    prevId = level.canonicalId
                }
                line = reader.readLine()
            }
            packData.add(pack)
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

    fun getLevelData(id: String): LevelMetadata? {
        if (id != "∞") {
            return levelData[id]
        }
        return LevelMetadata(
            null,
            "∞",
            "∞",
            1000000,
            1000000,
            1000000
        )
    }

    companion object : SingletonHolder<MetadataSingleton, Context>(::MetadataSingleton)
}