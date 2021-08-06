package com.joshmermelstein.loopoverplus

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.math.min

// Struct for what defines a level
class GameplayParams(
    val id: String,
    val numRows: Int,
    val numCols: Int,
    val moveFactory: MoveFactory,
    val initial: Array<String>,
    val goal: Array<String>,
    val tutorialText: String
)

// Struct for passing around save files
class SavedLevel(
    val initial: Array<String>,
    val board: Array<String>,
    val goal: Array<String>,
    val undoStack: List<LegalMove>,
    val redoStack: List<LegalMove>,
    val numMoves: Int
)

// Manages the logic of the the game and produces frames to be rendered on demand.
class GameManager(
    private val params: GameplayParams,
    private val context: AppCompatActivity,
    private val buttonState: ButtonState
) {
    private val data = GameCellMetadata(context)

    // Represents the current state of the board except for any moves that are currently evaluating.
    var board = GameBoard(params.numRows, params.numCols, params.initial, data)

    // Represents the state of the board after all moves in the queue have been evaluated. Needed to
    // tell whether moves will be valid before they are enqueued.
    private var future = GameBoard(params.numRows, params.numCols, params.initial, data)
    private var goal = GameBoard(params.numRows, params.numCols, params.goal, data)

    private var moveQueue: MoveQueue = MoveQueue()
    private var undoStack = Stack<LegalMove>()
    private var redoStack = Stack<LegalMove>()

    private var numMoves: Int = 0
    private var complete = false

    private var highlights: Array<Highlight> = emptyArray()

    init {
        buttonState.undoButtonEnabled = false
        buttonState.redoButtonEnabled = false
        updateGameplayMoveCount()
    }

    fun loadFromSavedLevel(level: SavedLevel) {
        // If this save file represents a solved board don't re-popup the win dialog
        if (level.board.contentEquals(params.goal)) {
            return
        }

        this.board = GameBoard(params.numRows, params.numCols, level.board, data)
        this.future = GameBoard(params.numRows, params.numCols, level.board, data)

        this.undoStack.addAll(level.undoStack)
        buttonState.undoButtonEnabled = this.undoStack.isNotEmpty()
        this.redoStack.addAll(level.redoStack)
        buttonState.redoButtonEnabled = this.redoStack.isNotEmpty()
        this.numMoves = level.numMoves
        updateGameplayMoveCount()
    }

    fun reset() {
        complete = false
        moveQueue.reset()
        undoStack.removeAllElements()
        redoStack.removeAllElements()
        loadFromSavedLevel(
            SavedLevel(
                params.initial,
                params.initial,
                params.goal,
                emptyList(),
                emptyList(),
                0
            )
        )
    }

    // Updates the draw positions of game elements and checks for wins. Uses system time to
    // determine positions so that animation speed doesn't depend on frames per second.
    fun update() {
        moveQueue.runMoves(System.nanoTime(), this)
        if (!complete && this.isSolved()) {
            complete = true
            winDialog()
        }
    }

    // Helper for drawing a grid of cells to a bounding box on the canvas.
    private fun drawGrid(
        canvas: Canvas,
        bounds: Bounds,
        grid: GameBoard,
        padding: Int
    ) {
        for (row in 0 until grid.numRows) {
            for (col in 0 until grid.numCols) {
                grid.getCell(row, col).drawSelf(canvas, bounds, padding)
            }
        }
    }

    fun drawBoard(canvas: Canvas, boundsBoard: Bounds) {
        drawGrid(canvas, boundsBoard, board, 5)
    }

    fun drawGoal(canvas: Canvas, boundsGoal: Bounds) {
        drawGrid(canvas, boundsGoal, goal, 2)
    }

    fun drawHighlights(canvas: Canvas, boundsBoard: Bounds) {
        for (highlight in this.highlights) {
            highlight.drawSelf(
                canvas,
                boundsBoard,
                context,
                params.numRows,
                params.numCols
            )
        }
    }

    private fun isSolved(): Boolean {
        return future == goal
    }

    fun enqueueMove(axis: Axis, direction: Direction, offset: Int) {
        // We pass the future state of the board when creating Moves to make sure they will be valid
        // relative to the board at the time they start running.
        val move: Move = params.moveFactory.makeMove(axis, direction, offset, future)

        // Both legal and illegal moves get animated
        moveQueue.addMove(move)

        // But only legal moves get added to the undo stack and counted toward the user's numMoves
        if (move is LegalMove) {
            val wasSolved = isSolved()
            move.finalize(future)
            undoStack.push(move)
            redoStack.clear()
            numMoves++
            buttonState.undoButtonEnabled = true
            buttonState.redoButtonEnabled = false
            updateGameplayMoveCount()
            if (wasSolved) {
                complete = false
            }
        }
    }

    fun undoMove() {
        if (undoStack.empty()) {
            return
        }
        val wasSolved = isSolved()
        val lastMove = undoStack.peek().inverse()
        lastMove.finalize(future)
        moveQueue.addMove(lastMove)
        redoStack.push(undoStack.pop())
        numMoves--
        buttonState.redoButtonEnabled = true
        if (undoStack.empty()) {
            buttonState.undoButtonEnabled = false
        }
        updateGameplayMoveCount()
        if (wasSolved) {
            complete = false
        }
    }

    fun redoMove() {
        if (redoStack.empty()) {
            return
        }
        val wasSolved = isSolved()
        val redoneMove = redoStack.pop()
        redoneMove.finalize(future)
        moveQueue.addMove(redoneMove)
        undoStack.push(redoneMove)
        numMoves++
        buttonState.undoButtonEnabled = true
        if (redoStack.empty()) {
            buttonState.redoButtonEnabled = false
        }
        updateGameplayMoveCount()
        if (wasSolved) {
            complete = false
        }
    }

    fun addHighlights(axis: Axis, direction: Direction, offset: Int) {
        this.highlights = params.moveFactory.makeHighlights(axis, direction, offset, board)
    }

    fun resetHighlights() {
        this.highlights = emptyArray()
    }

    // Generates a human readable string explaining the rules of the level
    fun helpText(): String {
        return params.moveFactory.helpText()
    }

    // Updates the activity's UI
    private fun updateGameplayMoveCount() {
        context.findViewById<TextView>(R.id.gameplay_move_count)?.text =
            context.getString(R.string.moveCounter, numMoves)
        // This invalidation also refreshes the when the undo and redo buttons are enabled in the UI
        context.invalidateOptionsMenu()
    }

    // Used for generating save files.
    override fun toString(): String {
        val undo: String = undoStack.joinToString(",") { it.toString() }
        val redo: String = redoStack.joinToString(",") { it.toString() }
        val initial = params.initial.joinToString(",")

        return "$initial\n$future\n$goal\n$undo\n$redo\n$numMoves"
    }

    // Returns a string representing the user's undo stack so they can copy their solution and
    // paste it places.
    fun toUserString(): String {
        return undoStack.joinToString(" ") { it.toUserString() }
    }

    // Highscores for each level are stored in shared preferences.
    private fun updateHighscores(highscores: SharedPreferences) {
        with(highscores.edit()) {
            putInt(params.id, numMoves)
            commit()
        }
    }

    // Displays a dialog when the user wins.
    private fun winDialog() {
        val levelData: LevelMetadata =
            MetadataSingleton.getInstance(context).getLevelData(params.id) ?: return

        // compute the number of stars the player earned and whether it is a new highscore.
        val fourStar = levelData.fourStar
        val threeStar = levelData.threeStar
        val twoStar = levelData.twoStar
        val highscores: SharedPreferences =
            context.getSharedPreferences("highscores", Context.MODE_PRIVATE)
        val oldHighscore = highscores.getInt(params.id, Int.MAX_VALUE)

        val dialog = Dialog(context)
        dialog.setContentView(R.layout.win_popup)

        dialog.findViewById<TextView>(R.id.moveCount).text = when {
            oldHighscore > numMoves -> {
                updateHighscores(highscores)
                context.getString(
                    R.string.winDialogWonIn,
                    pluralizedMoves(numMoves, context)
                ) + context.getString(R.string.winDialogNewBest)
            }
            oldHighscore == numMoves -> context.getString(
                R.string.winDialogWonIn,
                pluralizedMoves(numMoves, context)
            ) + context.getString(R.string.winDialogTieBest)
            else -> context.getString(
                R.string.winDialogWonIn,
                pluralizedMoves(numMoves, context)
            ) + context.getString(R.string.winDialogNotBest, pluralizedMoves(oldHighscore, context))
        }

        // Award stars based on the score of this attempt
        val doBetter = dialog.findViewById<TextView>(R.id.doBetter)
        val earnedStars = dialog.findViewById<TextView>(R.id.earnedStars)
        earnedStars.text = when {
            numMoves <= fourStar -> "✯✯✯"
            numMoves <= threeStar -> "★★★"
            numMoves <= twoStar -> "★★☆"
            else -> "★☆☆"
        }

        // Print the threshold for more stars based on their best ever
        val newHighscore = min(oldHighscore, numMoves)
        doBetter.text = when {
            newHighscore <= fourStar -> context.getString(R.string.dialogPerfect)
            newHighscore <= threeStar -> context.getString(
                R.string.dialogWantFourStar,
                pluralizedMoves(fourStar, context)
            )
            newHighscore <= twoStar -> context.getString(
                R.string.dialogWantThreeStar,
                pluralizedMoves(threeStar, context)
            )
            else -> context.getString(
                R.string.dialogWantTwoStar,
                pluralizedMoves(twoStar, context)
            )
        }

        dialog.findViewById<Button>(R.id.menu).setOnClickListener {
            context.finish()
        }
        dialog.findViewById<Button>(R.id.retry).setOnClickListener {
            reset()
            dialog.dismiss()
        }
        val next = dialog.findViewById<Button>(R.id.next)
        if (levelData.next == null) {
            next.visibility = View.GONE
        } else {
            next.setOnClickListener {
                val intent = Intent(context, GameplayActivity::class.java)
                intent.putExtra("id", levelData.next)
                dialog.dismiss()
                context.startActivity(intent)
                context.finish()
            }
        }

        dialog.show()
    }
}