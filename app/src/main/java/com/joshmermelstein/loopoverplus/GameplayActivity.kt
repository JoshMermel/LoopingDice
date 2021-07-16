package com.joshmermelstein.loopoverplus

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import java.io.BufferedReader
import java.io.InputStreamReader


// An activity for displaying the lifetime of a level as well as other UI elements.
class GameplayActivity : AppCompatActivity() {
    // The id is not known until it is read from intent in OnCreate
    // This is the canonical ID of the level (i.e. "a_caged_flip"); not the display ID (i.e. "A10")
    private lateinit var id: String

    // The gameManager is created based on the id so it must be lateInit as well.
    private lateinit var gameManager: GameManager

    // Shared state with the game manager
    private var buttonState = ButtonState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameplay)

        when {
            intent.hasExtra("id") -> {
                // The normal way of creating a level - looking up its params based on an id
                this.id = intent.getStringExtra("id") ?: return
                createFromId(id)
            }
            intent.hasExtra("randomLevelParams") -> {
                // A secondary way of making a level. Generating GameplayParams based on a
                // RandomLevelParams struct
                val randomParams =
                    intent.getParcelableExtra<RandomLevelParams>("randomLevelParams") ?: return

                // For random levels, we give them a unique ID based on params so we can look up their saves later.
                this.id = "∞$randomParams"

                createFromRandomParams(randomParams)
            }
            else -> {
                Toast.makeText(
                    applicationContext,
                    "Tried to create gameplay activity without either kind of intent!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun createFromId(id: String) {
        val params = loadInitialLevel(id)
        if (params == null) {
            Toast.makeText(
                applicationContext,
                "Failed to load level params for $id",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        val save = loadSavedLevel(id, params.numRows, params.numCols)?.let {
            if (sameElements(it.board, params.goal)) it else null
        }

        createFromParams(params, save)
    }

    private fun createFromRandomParams(randomParams: RandomLevelParams) {
        // Get the save from a file if one exists and the user wants it.
        val savedLevel = loadSavedLevel(id, randomParams.numRows, randomParams.numCols)?.let {
            if (intent.getBooleanExtra("loadSave", false)) it else null
        }

        // Create gameplay params. If there is a save, pass its initial and goal states so they
        // overwrite the randomly generated initial/goal. This is necessary so that resetting works
        // properly for random levels loaded from saves.
        val gameplayParams =
            generateRandomLevel(randomParams, this, savedLevel?.initial, savedLevel?.goal)

        createFromParams(gameplayParams, savedLevel)
    }

    // Finishes initializing the gameplay activity based on a gameplay params
    private fun createFromParams(params: GameplayParams, save: SavedLevel?) {
        // Load the level and possibly load the saved state
        this.gameManager = GameManager(params, this, buttonState)
        if (save != null) {
            this.gameManager.loadFromSavedLevel(save)
        }

        // Set tutorial text
        val tutorialTextBoxWidth = Resources.getSystem().displayMetrics.heightPixels / 4
        val tutorialTextBoxHeight =
            Resources.getSystem().displayMetrics.widthPixels - tutorialTextBoxWidth
        findViewById<TextView>(R.id.tutorialText).apply {
            layoutParams.height = tutorialTextBoxWidth * 7 / 8
            layoutParams.width = tutorialTextBoxHeight * 7 / 8
            text = params.tutorialText
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this, 1, 200, 1,
                TypedValue.COMPLEX_UNIT_DIP
            )
        }

        findViewById<GameplayView>(R.id.gameplayView).apply {
            gameManager = this@GameplayActivity.gameManager
        }
        findViewById<Toolbar>(R.id.gameplay_toolbar).also {
            setSupportActionBar(it)
        }

        supportActionBar?.title =
            "#" + MetadataSingleton.getInstance(this).getLevelData(id)?.displayId
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::gameManager.isInitialized) {
            saveLevel(id, gameManager.toString())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.gameplay_dropdown, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.toolbar_undo)?.isEnabled = buttonState.undoButtonEnabled
        menu?.findItem(R.id.toolbar_redo)?.isEnabled = buttonState.redoButtonEnabled
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.copy -> {
                copyToClipboard()
                true
            }
            R.id.reset -> {
                gameManager.reset()
                true
            }
            R.id.level_help -> {
                helpDialog()
                true
            }
            R.id.toolbar_undo -> {
                gameManager.undoMove()
                true
            }
            R.id.toolbar_redo -> {
                gameManager.redoMove()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun copyToClipboard() {
        val moves: String = gameManager.toUserString()
        if (moves.isEmpty()) {
            Toast.makeText(applicationContext, "Nothing to copy", Toast.LENGTH_SHORT).show()
        } else {
            val clipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Moves", moves)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(
                applicationContext,
                "Copied " + when (moves.length) {
                    in 0..20 -> moves
                    else -> (moves.subSequence(0, 20)).toString() + "..."
                },
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Pops up a dialog with info to help the user understand the level.
    private fun helpDialog() {
        val levelData: LevelMetadata =
            MetadataSingleton.getInstance(this).getLevelData(id) ?: return
        // compute the number of stars the player earned and whether it is a new highscore.
        val fourStar = levelData.fourStar
        val threeStar = levelData.threeStar
        val twoStar = levelData.twoStar
        val highscore =
            getSharedPreferences("highscores", Context.MODE_PRIVATE).getInt(id, Int.MAX_VALUE)

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.help_popup)

        dialog.findViewById<TextView>(R.id.help_dialog_rules).apply {
            text = gameManager.helpText()
        }

        dialog.findViewById<TextView>(R.id.help_dialog_pb).apply {
            if (highscore != Int.MAX_VALUE) {
                text = "Your best score on this level was $highscore " + pluralizedMoves(
                    highscore
                )
            } else {
                isVisible = false
            }
        }

        dialog.findViewById<TextView>(R.id.help_dialog_stars).apply {
            text = when {
                highscore == Int.MAX_VALUE -> "Win in any number of moves to earn a star"
                highscore > twoStar -> "Win in $twoStar " + pluralizedMoves(twoStar) + " to earn two stars"
                highscore > threeStar -> "Win in $threeStar " + pluralizedMoves(threeStar) + " to earn three stars"
                highscore > fourStar -> "A perfect score is $fourStar " + pluralizedMoves(
                    fourStar
                )
                else -> "You've earned all possible stars!"
            }
        }

        dialog.show()
    }

    private fun loadInitialLevel(id: String): GameplayParams? {
        try {
            val reader = BufferedReader(InputStreamReader(assets.open("levels/$id.txt")))

            val numRows: Int = reader.readLine().toInt()
            val numCols: Int = reader.readLine().toInt()
            val factory: MoveFactory = makeMoveFactory(reader.readLine())
            val initial: Array<String> = reader.readLine().split(",").toTypedArray()
            val final: Array<String> = reader.readLine().split(",").toTypedArray()
            val tutorialText: String = reader.readLine() ?: ""
            reader.close()
            return GameplayParams(
                id,
                numRows,
                numCols,
                factory,
                initial,
                final,
                tutorialText
            )
        } catch (e: Exception) {
        }
        return null
    }

    private fun loadSavedLevel(id: String, numRows: Int, numCols: Int): SavedLevel? {
        try {
            val reader = openFileInput("$id.txt").bufferedReader()
            val initial: Array<String> = reader.readLine().split(",").toTypedArray()
            val board: Array<String> = reader.readLine().split(",").toTypedArray()
            val goal: Array<String> = reader.readLine().split(",").toTypedArray()
            val undoStack: List<LegalMove> =
                reader.readLine().split(",").mapNotNull { stringToMove(it, numRows, numCols) }
            val redoStack: List<LegalMove> =
                reader.readLine().split(",").mapNotNull { stringToMove(it, numRows, numCols) }
            val numMoves: Int = reader.readLine().toInt()

            return SavedLevel(initial, board, goal, undoStack, redoStack, numMoves)
        } catch (e: Exception) {
        }
        return null
    }

    private fun saveLevel(id: String, contents: String) {
        openFileOutput("$id.txt", Context.MODE_PRIVATE).use {
            it.write(contents.toByteArray())
        }
    }
}

// The gameplay manager needs to control some state on this activity. Rather than letting it
// manipulate state directly, we use a reference to one of these structs to pass shared state.
class ButtonState {
    var undoButtonEnabled = true
    var redoButtonEnabled = true
}