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

// Struct for what defines a level
class GameplayParams(
    val id: String,
    val numRows: Int,
    val numCols: Int,
    val moveFactory: MoveFactory,
    val initial: Array<String>,
    val goal: Array<String>
)

// Struct for passing around save files
class SavedLevel(
    val board: Array<String>,
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
    // Represents the current state of the board except for any moves that are currently evaluating.
    var board = makeBoard(params.numRows, params.numCols, params.initial)

    // Represents the state of the board after all moves in the queue have been evaluated. Needed to
    // tell whether moves will be valid before they are enqueued.
    private var future = makeBoard(params.numRows, params.numCols, params.initial)
    private var goal = makeBoard(params.numRows, params.numCols, params.goal)

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
            complete = true
        }

        this.board = makeBoard(params.numRows, params.numCols, level.board)
        this.future = makeBoard(params.numRows, params.numCols, level.board)
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
        loadFromSavedLevel(SavedLevel(params.initial, emptyList(), emptyList(), 0))
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
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        grid: GameBoard,
        padding: Int
    ) {
        for (row in 0 until grid.numRows) {
            for (col in 0 until grid.numCols) {
                grid.getCell(row, col).drawSelf(canvas, left, top, right, bottom, padding)
            }
        }
    }

    fun drawBoard(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int) {
        drawGrid(canvas, left, top, right, bottom, board, 10)
    }

    fun drawGoal(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int) {
        drawGrid(canvas, left, top, right, bottom, goal, 2)
    }

    fun drawHighlights(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int) {
        for (highlight in this.highlights) {
            highlight.drawSelf(
                canvas,
                left,
                top,
                right,
                bottom,
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
            move.updateGrid(future)
            undoStack.push(move)
            redoStack.clear()
            numMoves++
            buttonState.undoButtonEnabled = true
            buttonState.redoButtonEnabled = false
            updateGameplayMoveCount()
        }
    }

    fun undoMove() {
        if (undoStack.empty()) {
            return
        }
        val wasSolved = isSolved()
        val lastMove = undoStack.peek().inverse()
        lastMove.updateGrid(future)
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
        val redoneMove = redoStack.pop()
        redoneMove.updateGrid(future)
        moveQueue.addMove(redoneMove)
        undoStack.push(redoneMove)
        numMoves++
        buttonState.undoButtonEnabled = true
        if (redoStack.empty()) {
            buttonState.redoButtonEnabled = false
        }
        updateGameplayMoveCount()
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
        val gameplayMoveCount = context.findViewById<TextView>(R.id.gameplay_move_count)
        gameplayMoveCount.text = "Moves: $numMoves"
        // This invalidation also refreshes the when the undo and redo buttons are enabled in the UI
        context.invalidateOptionsMenu()
    }

    // Used for generating save files.
    override fun toString(): String {
        val undo: String = undoStack.joinToString(",") { it.toString() }
        val redo: String = redoStack.joinToString(",") { it.toString() }

        return "$future\n$undo\n$redo\n$numMoves"
    }

    // Returns a string representing the user's undo stack so they can copy their solution and
    // paste it places.
    fun toUserString() : String {
        return undoStack.joinToString(" ") { it.toUserString() }
    }

    // Helper for converting an array of ColorIds into a board.
    private fun makeBoard(
        numRows: Int,
        numCols: Int,
        contents: Array<String>
    ): GameBoard {
        return GameBoard(
            Array(numRows) { row ->
                Array(numCols) { col ->
                    makeGameCell(
                        col,
                        row,
                        params,
                        contents[row * params.numCols + col],
                        context
                    )
                }
            })
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
        val threeStar = levelData.threeStar
        val twoStar = levelData.twoStar
        val highscores: SharedPreferences =
            context.getSharedPreferences("highscores", Context.MODE_PRIVATE)
        val oldHighscore = highscores.getInt(params.id, Int.MAX_VALUE)

        val dialog = Dialog(context)
        dialog.setContentView(R.layout.win_popup)

        val moveCount = dialog.findViewById<TextView>(R.id.moveCount)
        moveCount.text = when {
            oldHighscore > numMoves -> {
                updateHighscores(highscores)
                "You won in $numMoves " + pluralizedMoves(numMoves) + "- a new personal best!"
            }
            oldHighscore == numMoves -> {
                "You won in $numMoves " + pluralizedMoves(numMoves) + "- that ties your personal best"
            }
            else -> {
                "You won in $numMoves " + pluralizedMoves(numMoves) + "- your personal best is $oldHighscore " + pluralizedMoves(
                    oldHighscore
                )
            }
        }

        val doBetter = dialog.findViewById<TextView>(R.id.doBetter)
        val earnedStars = dialog.findViewById<TextView>(R.id.earnedStars)
        when {
            numMoves <= threeStar -> {
                earnedStars.text = "    ★★★    "
                doBetter.text = "You've earned all 3 stars!"
            }
            numMoves <= twoStar -> {
                earnedStars.text = "    ★★☆    "
                doBetter.text =
                    "Win in $threeStar " + pluralizedMoves(threeStar) + " to earn the final star"
            }
            else -> {
                earnedStars.text = "    ★☆☆    "
                doBetter.text =
                    "Win in $twoStar " + pluralizedMoves(twoStar) + " to earn another star"
            }
        }

        val menu = dialog.findViewById<Button>(R.id.menu)
        menu.setOnClickListener {
            context.finish()
        }
        val retry = dialog.findViewById<Button>(R.id.retry)
        retry.setOnClickListener {
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
                context.startActivity(intent)
                context.finish()
            }
        }

        dialog.show()
    }
}