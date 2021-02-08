package com.joshmermelstein.loopoverplus

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class LevelMetadata(
    var next: String?,
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
            "wide",
            "gear",
            "carousel",
            "static",
            "dynamic",
            "enabler",
            "bandaged"
        )) {
            val reader =
                BufferedReader(InputStreamReader(context.assets.open("packs/$filename.txt")))
            val title: String = reader.readLine()!!
            val pack = PackMetadata(title, mutableListOf())

            var line: String? = reader.readLine()
            while (line != null) {
                val parts = line.split(" ")
                pack.levels.add(parts[0])
                levelData[parts[0]] = LevelMetadata(null, parts[1].toInt(), parts[2].toInt())
                levelData[prevId]?.next = parts[0]
                prevId = parts[0]

                line = reader.readLine()
            }
            packData.add(pack)
            prevId = null
        }
    }

    fun getLevelData(id: String): LevelMetadata? {
        return levelData[id]
    }

    companion object : SingletonHolder<MetadataSingleton, Context>(::MetadataSingleton)
}