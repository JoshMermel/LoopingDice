package com.joshmermelstein.loopoverplus

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

// TODO(jmerm): think more about cases where we string split like loading levels in singleton or
//  loading save files and make sure they are safe

// The main activity for the app is a level select screen.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.level_select_toolbar))

        // Info on which levels exist and how to group them is lazily loaded into a global
        // singleton for easy lookup.
        for (pack in MetadataSingleton.getInstance(this).packData) {
            appendLevelPack(pack)
        }
    }

    // Because I am dumb and didn't use Room or something for my underlying storage, I need to
    // explicitly redraw all the buttons after a level completes in order to update the displayed
    // stars.
    override fun onResume() {
        redrawLevelSelect()
        super.onResume()
    }

    private fun redrawLevelSelect() {
        findViewById<LinearLayout>(R.id.LevelLinearLayout).removeAllViews()
        for (pack in MetadataSingleton.getInstance(this).packData) {
            appendLevelPack(pack)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.level_select_dropdown, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.resetAll -> {
                resetAllDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Helper for adding a group of levels to the level select screen.
    private fun appendLevelPack(pack: PackMetadata) {
        val metadata = MetadataSingleton.getInstance(this)
        val layout = findViewById<LinearLayout>(R.id.LevelLinearLayout)

        val title = TextView(this)
        title.text = pack.title
        title.setTextColor(Color.BLACK)
        title.setPadding(10, 10, 10, 10)
        layout.addView(title)

        var levelsInRow = 0
        var row = LinearLayout(this)
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        for (id in pack.levels) {
            val levelData = metadata.getLevelData(id) ?: continue
            val btnTag = Button(this)
            btnTag.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
            btnTag.text = buttonText(levelData)
            btnTag.setOnClickListener {
                val intent = Intent(this, GameplayActivity::class.java)
                intent.putExtra("id", id)
                startActivity(intent)
            }
            row.addView(btnTag)

            levelsInRow++
            if (levelsInRow % 4 == 0) {
                layout.addView(row)
                row = LinearLayout(this)
                row.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                levelsInRow = 0
            }
        }

        // To keep all boxes the same width, we add a few extra invisible boxes to the last row if
        // it wasn't already full.
        if (levelsInRow % 4 != 0) {
            for (j in (0..(3 - levelsInRow))) {
                val btnTag = Button(this)
                btnTag.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                btnTag.visibility = View.INVISIBLE
                btnTag.text = "\n"
                row.addView(btnTag)
            }
            layout.addView(row)
        }
    }

    // Figures out what text to write to a button based on looking up the user's highscore and
    // comparing it to par.
    private fun buttonText(levelData: LevelMetadata): String {
        val highscores: SharedPreferences = getSharedPreferences("highscores", Context.MODE_PRIVATE)
        val highscore = highscores.getInt(levelData.canonicalId, Int.MAX_VALUE)
        val id = levelData.displayId

        return when {
            highscore <= levelData.fourStar -> {
                "$id\n✯✯✯"
            }
            highscore <= levelData.threeStar -> {
                "$id\n★★★"
            }
            highscore <= levelData.twoStar -> {
                "$id\n★★☆"
            }
            highscore < Int.MAX_VALUE -> {
                "$id\n★☆☆"
            }
            else -> {
                "$id\n☆☆☆"
            }
        }
    }

    // A dialog to make sure the user really wants to delete all their saved data.
    private fun resetAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset")
            .setMessage("Are you sure? This will reset progress on all levels and reset all stars.")
            .setPositiveButton("Yes (delete it all)") { _, _ ->
                clearSharedPreferences()
                deleteSaves()
                redrawLevelSelect()
//                findViewById<View>(R.id.LevelLinearLayout).invalidate()

            }
            .setNegativeButton("no", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    // Deletes all shared preferences which is where highscores are stored.
    private fun clearSharedPreferences() {
        val dir = this.filesDir.parent ?: return
        val subdir = File("$dir/shared_prefs/")
        val children: Array<String> = subdir.list() ?: return
        for (i in children.indices) {
            // clear each preference file
            this.getSharedPreferences(children[i].replace(".xml", ""), Context.MODE_PRIVATE).edit()
                .clear().apply()
            //delete the file
            File(subdir, children[i]).delete()
        }
    }

    // Deletes all per-level save files.
    private fun deleteSaves() {
        val dir: File = this.filesDir
        val files: Array<File> = dir.listFiles() ?: return
        for (file in files) {
            file.delete()
        }
    }
}

