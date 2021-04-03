package com.joshmermelstein.loopoverplus

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader

// An activity for displaying the lifetime of a level as well as other UI elements.
class GameplayActivity : AppCompatActivity() {
    // The id is not known until it is read from intent in OnCreate
    private lateinit var id: String

    // The gameManager is created based on the id so it must be lateInit as well.
    private lateinit var gameManager: GameManager

    // Shared state with the game manager
    private var buttonState = ButtonState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_gameplay)
        val gameplayView = findViewById<GameplayView>(R.id.gameplayView)

        this.id = intent.getStringExtra("id") ?: return

        val params = loadInitialLevel(id)
        this.gameManager = GameManager(params, this, buttonState)
        val save = loadSavedLevel(id, params.numRows, params.numCols)
        if (save != null && save.board.size == params.numRows * params.numCols) {
            this.gameManager.loadFromSavedLevel(save)
        }

        gameplayView.gameManager = this.gameManager
        val toolbar = findViewById<Toolbar>(R.id.gameplay_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title =
            "#" + MetadataSingleton.getInstance(this).getLevelData(id)?.displayId
    }

    override fun onDestroy() {
        super.onDestroy()
        saveLevel(id, gameManager.toString())
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
                val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Moves", gameManager.toUserString())
                clipboardManager.setPrimaryClip(clip)
                // TODO(jmerm): do a toast here to let them know it succeeded?
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

    // Pops up a dialog with info to help the user understand the level.
    private fun helpDialog() {
        val levelData: LevelMetadata =
            MetadataSingleton.getInstance(this).getLevelData(id) ?: return
        // compute the number of stars the player earned and whether it is a new highscore.
        val fourStar = levelData.fourStar
        val threeStar = levelData.threeStar
        val twoStar = levelData.twoStar
        val highscores: SharedPreferences =
            getSharedPreferences("highscores", Context.MODE_PRIVATE)
        val oldHighscore = highscores.getInt(id, Int.MAX_VALUE)

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.help_popup)

        val rules = dialog.findViewById<TextView>(R.id.help_dialog_rules)
        rules.text = gameManager.helpText()

        val pb = dialog.findViewById<TextView>(R.id.help_dialog_pb)
        if (highscores.contains(id)) {
            pb.text =
                "Your best score on this level was $oldHighscore " + pluralizedMoves(oldHighscore)
        } else {
            pb.isVisible = false
        }

        val stars = dialog.findViewById<TextView>(R.id.help_dialog_stars)
        stars.text = when {
            oldHighscore == Int.MAX_VALUE -> {"Win in any number of moves to earn a star"}
            oldHighscore > twoStar -> { "Win in $twoStar " + pluralizedMoves(twoStar) + " to earn two stars"}
            oldHighscore > threeStar -> { "Win in $threeStar " + pluralizedMoves(threeStar) + " to earn three stars" }
            oldHighscore > fourStar -> { "A perfect score is $fourStar" + pluralizedMoves(fourStar) }
            else -> { "You've earned all possible stars" }
        }

        dialog.show()
    }

    private fun loadInitialLevel(id: String): GameplayParams {
        // TODO(jmerm): handle missing or incorrectly formatted file
        val reader = BufferedReader(InputStreamReader(assets.open("levels/$id.txt")))

        val numRows: Int = reader.readLine().toInt()
        val numCols: Int = reader.readLine().toInt()
        val factory: MoveFactory = makeMoveFactory(reader.readLine())
        val initial: Array<String> = reader.readLine().split(",").toTypedArray()
        val final: Array<String> = reader.readLine().split(",").toTypedArray()
        reader.close()
        return GameplayParams(id, numRows, numCols, factory, initial, final)
    }

    private fun loadSavedLevel(id: String, numRows: Int, numCols: Int): SavedLevel? {
        // TODO(jmerm): better handling for malformed files.
        try {
            val reader = openFileInput("$id.txt").bufferedReader()
            val board: Array<String> = reader.readLine().split(",").toTypedArray()
            val undoStack: List<LegalMove> =
                reader.readLine().split(",").mapNotNull { stringToMove(it, numRows, numCols) }
            val redoStack: List<LegalMove> =
                reader.readLine().split(",").mapNotNull { stringToMove(it, numRows, numCols) }
            val numMoves: Int = reader.readLine().toInt()

            return SavedLevel(board, undoStack, redoStack, numMoves)
        } catch (e: FileNotFoundException) {
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