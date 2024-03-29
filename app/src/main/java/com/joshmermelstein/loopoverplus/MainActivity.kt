package com.joshmermelstein.loopoverplus


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File

// The main activity for the app.
// Shows the user all of the packs of levels and lets them pick which one they want to play.
// Also shows the user an button to launch the infinity activity.
class MainActivity : AppCompatActivity() {
    // Because I'm dumb, I have to redraw the entire UI on reload to pick up new star
    // number/completion numbers. This set keeps track of which expandos are expanded so I can
    // reopen them and make the UI look more continuous.
    private val expandedListItems = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.pack_select_toolbar))
        redrawLevelSelect()
        findViewById<ScrollView>(R.id.PackScrollView).isSmoothScrollingEnabled = true
    }

    // Because I am dumb and didn't use Room or something for my underlying storage, I need to
    // explicitly redraw all the buttons after a level completes in order to update the displayed
    // stars.
    override fun onResume() {
        redrawLevelSelect()
        super.onResume()
    }

    private fun redrawLevelSelect() {
        findViewById<LinearLayout>(R.id.PackLinearLayout).removeAllViews()

        // Info on which levels exist and how to group them is lazily loaded into a global
        // singleton for easy lookup.
        MetadataSingleton.getInstance(this).packData.keys.forEach {
            appendLevelPack(it)
        }
        // The infinity button goes to a level builder activity where the user can generate a level
        // on the fly.
        appendInfinityExpando()
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
    private fun appendLevelPack(packId: String) {
        // Ensure we have data to lay out.
        val levels = MetadataSingleton.getInstance(this).packData[packId] ?: return

        // Create a list item to hold views related to this level pack
        val layout = findViewById<LinearLayout>(R.id.PackLinearLayout)


        val header = makeHeader(packId)
        val levelsContainer = makeLevelButtons(packId, levels)

        // Configure onclick listener to move to Level Select activity for this pack ID.
        header.setOnClickListener {
            levelsContainer.visibility =
                if (levelsContainer.visibility == View.GONE) {
                    expandedListItems.add(packId)
                    View.VISIBLE
                } else {
                    expandedListItems.remove(packId)
                    View.GONE
                }
            // Focus the second row of the expanded pack. This scrolls a bit if the pack was at the
            // bottom of the screen but not a jarring amount of the container being expanded is
            // larger than the screen.
            layout.requestChildFocus(
                header,
                levelsContainer.getChildAt((levelsContainer.childCount - 1).coerceAtMost(1))
            )
        }

        // Put header and buttons in a vertical linear layout and add that to the activity's layout.
        LinearLayout(this).also {
            it.orientation = LinearLayout.VERTICAL
            it.addView(header)
            it.addView(levelsContainer)
            layout.addView(it)
        }
        addDivider(layout)
    }

    private fun makeHeader(packId: String): LinearLayout {
        // Create a layout row for views related to this pack.
        val header = LinearLayout(this)
        header.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Write the name of the pack
        TextView(this).also {
            it.text = packId
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
            it.setPadding(0, 0, 0, 25)
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1F
            )
            header.addView(it)
        }

        // Write how much of the pack the user has beaten.
        TextView(this).also {
            it.text = MetadataSingleton.getInstance(this@MainActivity).getNumComplete(packId)
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            header.addView(it)
        }

        // configure onclick UI effect
        addOnclickEffect(header)

        return header
    }

    private fun makeLevelButtons(packId: String, levels: List<String>): LinearLayout {
        val numCols = mainScreenNumCols
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val metadata = MetadataSingleton.getInstance(this)

        var levelsInRow = 0
        var row = LinearLayout(this)
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        for (id in levels) {
            val levelData = metadata.getLevelData(id) ?: continue
            val btnTag = Button(this)
            btnTag.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
            val numStars = numStars(levelData)
            btnTag.text = buttonText(levelData.displayId, numStars)
            btnTag.backgroundTintList = ColorStateList.valueOf(buttonColor(numStars))
            btnTag.setOnClickListener {
                val intent = Intent(this, GameplayActivity::class.java)
                intent.putExtra("id", id)
                startActivity(intent)
            }
            row.addView(btnTag)

            levelsInRow++
            if (levelsInRow % numCols == 0) {
                buttonContainer.addView(row)
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
        if (levelsInRow % numCols != 0) {
            repeat(numCols - levelsInRow) {
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
            buttonContainer.addView(row)
        }
        if (!expandedListItems.contains(packId)) {
            buttonContainer.visibility = View.GONE
        }
        return buttonContainer
    }

    private fun numStars(levelData: LevelMetadata): Int {
        val highscores: SharedPreferences = getSharedPreferences("highscores", Context.MODE_PRIVATE)
        val highscore = highscores.getInt(levelData.canonicalId, Int.MAX_VALUE)

        return when {
            highscore <= levelData.fourStar -> 4
            highscore <= levelData.threeStar -> 3
            highscore <= levelData.twoStar -> 2
            highscore < Int.MAX_VALUE -> 1
            else -> 0
        }
    }

    // Figures out what text to write to a button based on looking up the user's highscore and
    // comparing it to par.
    private fun buttonText(id: String, numStars: Int): String {
        return id + "\n" + when (numStars) {
            4 -> getString(R.string.fourStars)
            3 -> getString(R.string.threeStars)
            2 -> getString(R.string.twoStars)
            1 -> getString(R.string.oneStar)
            else -> getString(R.string.noStars)
        }
    }

    private fun buttonColor(numStars: Int): Int {
        return if (numStars > 0) {
            ContextCompat.getColor(this, R.color.completed_level)
        } else {
            ContextCompat.getColor(this, R.color.incomplete_level)
        }
    }

    // Add a horizontal line between rows of a linear layout for visual clarity.
    private fun addDivider(layout: LinearLayout) {
        val line = View(this).also {
            it.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                2
            )
            it.setBackgroundColor(getColor(R.color.bandaged_cell))
        }
        layout.addView(line)
    }

    private fun appendInfinityExpando() {
        val layout = findViewById<LinearLayout>(R.id.PackLinearLayout)

        // Create a layout row
        val header = LinearLayout(this)
        header.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        addOnclickEffect(header)

        // Write the name of the pack
        TextView(this).also {
            it.text = getString(R.string.infinityLabel)
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
            it.setPadding(0, 0, 0, 25)
            it.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1F
            )
            header.addView(it)
        }

        val buttonContainer = LinearLayout(this)

        // Level Builder
        Button(this).also {
            it.text = getString(R.string.levelSelectLevelBuilder)
            it.setOnClickListener {
                val intent = Intent(this, InfinityActivity::class.java)
                startActivity(intent)
            }
            it.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
            buttonContainer.addView(it)
        }

        // I'm feeling lucky
        Button(this).also {
            it.text = getString(R.string.levelSelectFeelingLucky)
            it.setOnClickListener {
                val intent = Intent(this, GameplayActivity::class.java)
                val params = feelingLucky()
                saveParamsToRecentLevels(
                    getSharedPreferences("RecentLevels", Context.MODE_PRIVATE),
                    params
                )
                intent.putExtra("randomLevelParams", params)
                intent.putExtra("loadSave", true)
                startActivity(intent)
            }
            it.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
            buttonContainer.addView(it)
        }

        // Configure onclick listener to move to Level Select activity for this pack ID.
        header.setOnClickListener {
            buttonContainer.visibility =
                if (buttonContainer.visibility == View.GONE) {
                    expandedListItems.add(getString(R.string.infinityLabel))
                    View.VISIBLE
                } else {
                    expandedListItems.remove(getString(R.string.infinityLabel))
                    View.GONE
                }
            // Unlike normal expandos, focus the button container here since it's appearing below
            // the screen and would be easy to miss otherwise.
            layout.requestChildFocus(header, buttonContainer)
        }


        // Put header and buttons in a vertical linear layout and add that to the activity's layout.
        LinearLayout(this).also {
            it.orientation = LinearLayout.VERTICAL
            it.addView(header)
            it.addView(buttonContainer)
            layout.addView(it)
        }

        if (!expandedListItems.contains(getString(R.string.infinityLabel))) {
            buttonContainer.visibility = View.GONE
        }
    }

    private fun addOnclickEffect(v: View) {
        val attrs = intArrayOf(android.R.attr.selectableItemBackground)
        val typedArray: TypedArray = obtainStyledAttributes(attrs)
        val backgroundResource = typedArray.getResourceId(0, 0)
        v.setBackgroundResource(backgroundResource)
        typedArray.recycle()
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
            }
            .setNegativeButton("no", null)
            .setIconAttribute(android.R.attr.alertDialogIcon)
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

