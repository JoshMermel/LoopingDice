package com.joshmermelstein.loopoverplus

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class LevelMetadata(
    var next: String?,
    var displayId: String,
    var canonicalId: String,
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
            "carousel",
            "dynamic",
            "bandaged",
            "gear",
            "static",
            "enabler",
            "hybrid_wc",
            "hybrid_wg",
            "hybrid_cg",
        )) {
            val reader =
                BufferedReader(InputStreamReader(context.assets.open("packs/$filename.txt")))
            val title: String = reader.readLine()!!
            val pack = PackMetadata(title, mutableListOf())

            var line: String? = reader.readLine()
            while (line != null) {
                // TODO(jmerm): handle invalid/empty input better here.
                val parts = line.split(" ")
                pack.levels.add(parts[1])
                levelData[parts[1]] =
                    LevelMetadata(null, parts[0], parts[1], parts[2].toInt(), parts[3].toInt())
                levelData[prevId]?.next = parts[1]
                prevId = parts[1]

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