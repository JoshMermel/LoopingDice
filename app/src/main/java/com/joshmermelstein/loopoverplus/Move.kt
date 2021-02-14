package com.joshmermelstein.loopoverplus

import android.util.Log
import kotlin.math.pow


// A move represents the outcome of the user swiping on the board. Most moves move cells around in three phases:
// - updating their draw positions
// - updating the underlying grid once they are done
// - finalizing their draw positions
interface Move {
    fun run(manager: GameManager, startTime: Long, endTime: Long, currentTime: Long) {
        // Compute how much of the move has happened
        val progress: Double = when {
            (currentTime < startTime) -> {
                // It's too soon to do anything
                return
            }
            (currentTime < endTime) -> {
                // We're in the middle of a move
                ease((currentTime - startTime) / (endTime - startTime).toDouble())
            }
            else -> {
                // The move is over, snap to 100% progress.
                1.0
            }
        }

        // Update the positions of relevant cells
        updatePositions(progress, manager.board)

        // if it's over, also finalize
        if (currentTime > endTime) {
            updateGrid(manager.board)
            finalize(manager.board)
        }
    }

    fun ease(x: Double): Double {
        // ease out quad
        //return 1 - (1 - x).pow(2)

        // easeOutBack
        val c1 = 1.70158
        val c3 = c1 + 1
        return 1 + c3 * (x - 1).pow(3) + c1 * (x - 1).pow(2)
    }


    // Updates the internal absolute position of each modified cell. This makes it possible to move
    // those cells again relative to their new position.
    fun finalize(board: Array<Array<GameCell>>) {
        val numRows = board.size
        val numCols = board[0].size
        for (row in board) {
            for (cell in row) {
                cell.finalize(numRows, numCols)
            }
        }
    }

    // Updates the draw position of cells based on |progress| but does not update the underlying
    // board
    fun updatePositions(progress: Double, board: Array<Array<GameCell>>)

    // Updates the underlying board once a move has completed so further moves will apply to the
    // correct cells
    fun updateGrid(board: Array<Array<GameCell>>)

    // Returns a move that undoes this one
    fun inverse(): Move

    // Whether this move is a legal one. Used to determine whether it is written to the undo stack,
    // counts toward the total moves, etc.
    val isLegal: Boolean

    // Used for saving move history to a file.
    override fun toString(): String
}

// TODO(jmerm): comment this.
interface CoordinatesMove : Move {
    val transitions: List<Transition>

    override fun updatePositions(progress: Double, board: Array<Array<GameCell>>) {
        val numRows = board.size
        val numCols = board[0].size
        for (t in transitions) {
            val cell = board[t.y0 % numRows][t.x0 % numCols]
            cell.offsetX = (t.x1 - t.x0) * progress
            cell.offsetY = (t.y1 - t.y0) * progress
        }
    }

    override fun updateGrid(board: Array<Array<GameCell>>) {
        val numRows = board.size
        val numCols = board[0].size

        // Read all updates into a map so they can be executed without overwriting each other.
        val updates: MutableMap<Transition, GameCell> = mutableMapOf()
        for (t in transitions) {
            updates[t] = board[t.y0 % numRows][t.x0 % numCols]
        }

        // Write those updates back into the board.
        for ((t, cell) in updates) {
            board[(t.y1 + numRows) % numRows][(t.x1 + numCols) % numCols] = cell
        }
    }
}

// Basic move moves a single row or column. It is handy as a base class for more complex kinds of
// Row/Col based moves.
open class BasicMove(
    open val axis: Axis,
    open var direction: Direction,
    open val offset: Int,
    open val numRows: Int,
    open val numCols: Int
) : CoordinatesMove {
    override val isLegal = true
    override val transitions = mutableListOf<Transition>()

    init {
        if (axis == Axis.HORIZONTAL && direction == Direction.FORWARD) {
            for (col in 0 until numCols) {
                transitions.add(Transition(col, offset, col + 1, offset))
            }
        } else if (axis == Axis.HORIZONTAL && direction == Direction.BACKWARD) {
            for (col in 0 until numCols) {
                transitions.add(Transition(col, offset, col - 1, offset))
            }
        } else if (axis == Axis.VERTICAL && direction == Direction.FORWARD) {
            for (row in 0 until numRows) {
                transitions.add(Transition(offset, row, offset, row + 1))
            }
        } else {
            for (row in 0 until numRows) {
                transitions.add(Transition(offset, row, offset, row - 1))
            }
        }
    }

    override fun inverse(): Move {
        return BasicMove(axis, opposite(direction), offset, numRows, numCols)
    }

    override fun toString(): String {
        return "BASIC " + axisToString(axis) + " " + directionToString(direction) + " $offset"
    }
}

// A wide move is like a basic move but it effects many rows/columns depending on depth.
class WideMove(
    private val axis: Axis,
    private var direction: Direction,
    private val offset: Int,
    private val numRows: Int,
    private val numCols: Int,
    private val depth: Int
) : CoordinatesMove {
    override val isLegal = true
    override val transitions = mutableListOf<Transition>()

    init {
        if (axis == Axis.HORIZONTAL && direction == Direction.FORWARD) {
            for (row in (offset until offset + depth)) {
                for (col in 0 until numCols) {
                    transitions.add(Transition(col, row % numRows, col + 1, row % numRows))
                }
            }
        } else if (axis == Axis.HORIZONTAL && direction == Direction.BACKWARD) {
            for (row in (offset until offset + depth)) {
                for (col in 0 until numCols) {
                    transitions.add(Transition(col, row % numRows, col - 1, row % numRows))
                }
            }
        } else if (axis == Axis.VERTICAL && direction == Direction.FORWARD) {
            for (col in (offset until offset + depth)) {
                for (row in 0 until numRows) {
                    transitions.add(Transition(col % numCols, row, col % numCols, row + 1))
                }
            }
        } else {
            for (col in (offset until offset + depth)) {
                for (row in 0 until numRows) {
                    transitions.add(Transition(col % numCols, row, col % numCols, row - 1))
                }
            }
        }
    }

    override fun inverse(): Move {
        return WideMove(axis, opposite(direction), offset, numRows, numCols, depth)
    }

    override fun toString(): String {
        return "WIDE " + axisToString(axis) + " " + directionToString(direction) + " $offset $depth"
    }
}

// A gear move is like a basic move but the row/col after the selected one also moves in the
// opposite direction.
class GearMove(
    private val axis: Axis,
    private var direction: Direction,
    private val offset: Int,
    private val numRows: Int,
    private val numCols: Int
) : CoordinatesMove {
    override val isLegal = true
    override val transitions = mutableListOf<Transition>()

    init {
        if (axis == Axis.HORIZONTAL && direction == Direction.FORWARD) {
            for (col in 0 until numCols) {
                transitions.add(Transition(col, offset, col + 1, offset))
                transitions.add(
                    Transition(
                        col,
                        (offset + 1) % numRows,
                        col - 1,
                        (offset + 1) % numRows
                    )
                )
            }
        } else if (axis == Axis.HORIZONTAL && direction == Direction.BACKWARD) {
            for (col in 0 until numCols) {
                transitions.add(Transition(col, offset, col - 1, offset))
                transitions.add(
                    Transition(
                        col,
                        (offset + 1) % numRows,
                        col + 1,
                        (offset + 1) % numRows
                    )
                )
            }
        } else if (axis == Axis.VERTICAL && direction == Direction.FORWARD) {
            for (row in 0 until numRows) {
                transitions.add(Transition(offset, row, offset, row + 1))
                transitions.add(
                    Transition(
                        (offset + 1) % numCols,
                        row,
                        (offset + 1) % numCols,
                        row - 1
                    )
                )
            }
        } else {
            for (row in 0 until numRows) {
                transitions.add(Transition(offset, row, offset, row - 1))
                transitions.add(
                    Transition(
                        (offset + 1) % numCols,
                        row,
                        (offset + 1) % numCols,
                        row + 1
                    )
                )
            }
        }
    }

    override fun inverse(): Move {
        return GearMove(axis, opposite(direction), offset, numRows, numCols)
    }

    override fun toString(): String {
        return "GEAR " + axisToString(axis) + " " + directionToString(direction) + " $offset"
    }
}

class Transition(
    val x0: Int,
    val y0: Int,
    val x1: Int,
    val y1: Int
)

// A Carousel move forms a ring with the row/col that was selected and it's neighbor and does a
// circular shift.
open class CarouselMove(
    private val axis: Axis,
    private var direction: Direction,
    private val offset: Int,
    private val numRows: Int,
    private val numCols: Int
) : CoordinatesMove {
    override val isLegal = true
    override val transitions = mutableListOf<Transition>()

    init {
        if (axis == Axis.HORIZONTAL) {
            fillTransitionsHorizontal(direction, offset, numCols)
        } else {
            fillTransitionsVertical(direction, offset, numRows)
        }
    }

    private fun fillTransitionsHorizontal(
        direction: Direction,
        offset: Int,
        numCols: Int
    ) {
        var rowRightIdx = offset
        var rowLeftIdx = offset
        if (direction == Direction.FORWARD) {
            rowLeftIdx += 1
        } else {
            rowRightIdx += 1
        }

        // Move one row right except the rightmost element
        for (col in (numCols - 1 downTo 1)) {
            transitions.add(Transition(col, rowLeftIdx, col - 1, rowLeftIdx))
        }
        // Move the rightmost element into the other row
        transitions.add(Transition(numCols - 1, rowRightIdx, numCols - 1, rowLeftIdx))

        // Move the other row left except the leftmost element
        for (col in (0 until numCols - 1)) {
            transitions.add(Transition(col, rowRightIdx, col + 1, rowRightIdx))
        }
        // Move the leftmost element into the other row
        transitions.add(Transition(0, rowLeftIdx, 0, rowRightIdx))
    }

    private fun fillTransitionsVertical(
        direction: Direction,
        offset: Int,
        numRows: Int
    ) {
        var colDownIdx = offset
        var colUpIdx = offset
        if (direction == Direction.FORWARD) {
            colUpIdx += 1
        } else {
            colDownIdx += 1
        }

        // Move all cells in one row down except the bottom one
        for (row in (numRows - 1 downTo 1)) {
            transitions.add(Transition(colUpIdx, row, colUpIdx, row - 1))
        }
        // Move the bottom cell of that row into the other column
        transitions.add(Transition(colDownIdx, numRows - 1, colUpIdx, numRows - 1))

        // Move all cells in the other row up except the top one
        for (row in (0 until numRows - 1)) {
            transitions.add(Transition(colDownIdx, row, colDownIdx, row + 1))
        }
        // Move the top cell of that row into the other column
        transitions.add(Transition(colUpIdx, 0, colDownIdx, 0))
    }

    override fun inverse(): Move {
        return CarouselMove(axis, opposite(direction), offset, numRows, numCols)
    }

    override fun toString(): String {
        return "CAROUSEL " + axisToString(axis) + " " + directionToString(direction) + " $offset"
    }
}

// A hack to signal when the user's input was received but results in an invalid move. This kind of
// move hooks into the moveQueue system to flash an icon to the user but does not moe around any cells.
class IllegalMove(private val cords: List<Pair<Int, Int>>) : Move {
    override val isLegal = false

    override fun updatePositions(progress: Double, board: Array<Array<GameCell>>) {
        for (cord in cords) {
            board[cord.first][cord.second].shouldDrawIcon = true
        }
    }

    override fun finalize(board: Array<Array<GameCell>>) {
        for (cord in cords) {
            board[cord.first][cord.second].shouldDrawIcon = false
        }
    }

    // Do nothing, the move is illegal
    override fun updateGrid(board: Array<Array<GameCell>>) {}

    // Should never happen, these moves shouldn't get pushed to the undo stack.
    override fun inverse(): Move {
        return this
    }

    // Should never happen, these should never be serialized.
    override fun toString(): String {
        return ""
    }
}

// Helper for loading saved moves from files.
fun stringToMove(s: String, numRows: Int, numCols: Int): Move? {
    val splits = s.split(" ")
    if (s.length < 3) {
        return null
    }
    val axis = if (splits[1] == "H") {
        Axis.HORIZONTAL
    } else {
        Axis.VERTICAL
    }
    val direction = if (splits[2] == "F") {
        Direction.FORWARD
    } else {
        Direction.BACKWARD
    }
    val offset = splits[3].toInt()

    return when (splits[0]) {
        "BASIC" -> {
            BasicMove(axis, direction, offset, numRows, numCols)
        }
        "WIDE" -> {
            WideMove(axis, direction, offset, numRows, numCols, splits[4].toInt())
        }
        "GEAR" -> {
            GearMove(axis, direction, offset, numRows, numCols)
        }
        "CAROUSEL" -> {
            CarouselMove(axis, direction, offset, numRows, numCols)
        }
        else -> null
    }
}

enum class Axis {
    VERTICAL, HORIZONTAL
}

enum class Direction(val dir: Int) {
    FORWARD(1),
    BACKWARD(-1)
}