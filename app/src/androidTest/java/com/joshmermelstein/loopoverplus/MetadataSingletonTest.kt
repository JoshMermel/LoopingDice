package com.joshmermelstein.loopoverplus

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase
import java.io.BufferedReader
import java.io.InputStreamReader

class MetadataSingletonTest : TestCase() {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val metadata = MetadataSingleton.getInstance(context)

    // Verifies that all referenced levels exist and all existing levels are referenced.
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

    fun testHelpTextIsDefined() {
        val referencedHelpTexts: MutableSet<String> = mutableSetOf()

        for (id in context.assets.list("levels")!!.toSet()) {
            val reader = BufferedReader(InputStreamReader(context.assets.open("levels/$id")))
            repeat(5) { reader.readLine() }
            val helptext = reader.readLine()
            if (helptext.isNotBlank()) {
                referencedHelpTexts.add(helptext)
            }
        }

        val actualHelpTexts = tutorialTextMap.keys

        // We could just assert that referenced and actual are equal but that gives unhelpful error
        // messages.
        referencedHelpTexts.forEach {
            assertTrue(
                "$it is referenced but does not exist",
                actualHelpTexts.contains(it)
            )
        }
        actualHelpTexts.forEach {
            assertTrue(
                "$it exists but is not referenced",
                referencedHelpTexts.contains(it)
            )
        }
    }
}