package com.joshmermelstein.loopoverplus

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase

class MetadataSingletonTest : TestCase() {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val metadata = MetadataSingleton.getInstance(context)

    fun testLevelFilesExist() {
        val referencedFiles: MutableSet<String> = mutableSetOf()
        for (pack in metadata.packData.values) {
            for (id in pack) {
                referencedFiles.add("$id.txt")
            }
        }

        val existingFiles = context.assets.list("levels")!!.toSet()

        // We could just assert that referencedFile and existingFiles are equal but that gives
        // unhelpful error messages.
        referencedFiles.forEach {
            assertTrue(
                "$it is referenced but does not exist",
                existingFiles.contains(it)
            )
        }
        existingFiles.forEach {
            assertTrue(
                "$it exists but is not referenced",
                referencedFiles.contains(it)
            )
        }

    }
}