package com.joshmermelstein.loopoverplus

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

// Basic move moves a single row or column. It is handy as a base class for more complex kinds of
// Row/Col based moves.
open class BasicMove(
    open val axis: Axis,
    open var direction: Direction,
    open val offset: Int
) : Move {
    override val isLegal = true

    override fun updatePositions(progress: Double, board: Array<Array<GameCell>>) {
        if (axis == Axis.HORIZONTAL) {
            updatePositionsRow(direction, offset, progress, board)
        } else {
            updatePositionsCol(direction, offset, progress, board)

        }
    }

    fun updatePositionsRow(
        direction: Direction,
        offset: Int,
        progress: Double,
        board: Array<Array<GameCell>>
    ) {
        for (cell in board[offset]) {
            cell.offsetX = (direction.dir * progress)
        }
    }

    fun updatePositionsCol(
        direction: Direction,
        offset: Int,
        progress: Double,
        board: Array<Array<GameCell>>
    ) {
        val numRows = board.size
        for (row in 0 until numRows) {
            board[row][offset].offsetY = (direction.dir * progress)
        }
    }

    override fun updateGrid(board: Array<Array<GameCell>>) {
        if (axis == Axis.HORIZONTAL) {
            updateGridRow(direction, offset, board)
        } else {
            updateGridCol(direction, offset, board)
        }
    }

    fun updateGridRow(direction: Direction, offset: Int, board: Array<Array<GameCell>>) {
        if (direction == Direction.FORWARD) {
            updateGridRowForward(offset, board)
        } else {
            updateGridRowBackward(offset, board)
        }
    }

    fun updateGridCol(direction: Direction, offset: Int, board: Array<Array<GameCell>>) {
        if (direction == Direction.FORWARD) {
            updateGridColForward(offset, board)
        } else {
            updateGridColBackward(offset, board)
        }
    }

    private fun updateGridRowForward(row: Int, board: Array<Array<GameCell>>) {
        val numCols = board[row].size
        val tmp: GameCell = board[row][numCols - 1]
        for (col in (numCols - 1 downTo 1)) {
            board[row][col] = board[row][col - 1]
        }
        board[row][0] = tmp
    }

    private fun updateGridRowBackward(row: Int, board: Array<Array<GameCell>>) {
        val numCols = board[row].size
        val tmp: GameCell = board[row][0]
        for (col in (0 until numCols - 1)) {
            board[row][col] = board[row][col + 1]
        }
        board[row][numCols - 1] = tmp
    }

    private fun updateGridColForward(col: Int, board: Array<Array<GameCell>>) {
        val numRows = board.size
        val tmp: GameCell = board[numRows - 1][col]
        for (row in (numRows - 1 downTo 1)) {
            board[row][col] = board[row - 1][col]
        }
        board[0][col] = tmp
    }

    private fun updateGridColBackward(col: Int, board: Array<Array<GameCell>>) {
        val numRows = board.size
        val tmp: GameCell = board[0][col]
        for (row in (0 until numRows - 1)) {
            board[row][col] = board[row + 1][col]
        }
        board[numRows - 1][col] = tmp
    }

    override fun inverse(): Move {
        return BasicMove(axis, opposite(direction), offset)
    }

    override fun toString(): String {
        return "BASIC " + axisToString(axis) + " " + directionToString(direction) + " $offset"
    }
}

// A wide move is like a basic move but it effects many rows/columns depending on depth.
class WideMove(
    override val axis: Axis,
    override var direction: Direction,
    override val offset: Int,
    private val depth: Int
) : BasicMove(axis, direction, offset) {
    override fun updatePositions(progress: Double, board: Array<Array<GameCell>>) {
        if (axis == Axis.HORIZONTAL) {
            updatePositionRows(direction, offset, progress, board, depth)
        } else {
            updatePositionCols(direction, offset, progress, board, depth)
        }
    }

    private fun updatePositionRows(
        direction: Direction,
        offset: Int,
        progress: Double,
        board: Array<Array<GameCell>>,
        depth: Int
    ) {
        val numRows = board.size
        for (row in (offset until offset + depth)) {
            updatePositionsRow(direction, row % numRows, progress, board)
        }
    }

    private fun updatePositionCols(
        direction: Direction,
        offset: Int,
        progress: Double,
        board: Array<Array<GameCell>>,
        depth: Int
    ) {
        val numCols = board[0].size
        for (col in (offset until offset + depth)) {
            updatePositionsCol(direction, col % numCols, progress, board)
        }
    }

    override fun updateGrid(board: Array<Array<GameCell>>) {
        if (axis == Axis.HORIZONTAL) {
            updateGridRows(board)
        } else {
            updateGridCols(board)
        }
    }

    private fun updateGridRows(board: Array<Array<GameCell>>) {
        val numRows = board.size
        for (row in (offset until offset + depth)) {
            updateGridRow(direction, row % numRows, board)
        }
    }

    private fun updateGridCols(board: Array<Array<GameCell>>) {
        val numCols = board[0].size
        for (col in (offset until offset + depth)) {
            updateGridCol(direction, col % numCols, board)
        }
    }

    override fun inverse(): Move {
        return WideMove(axis, opposite(direction), offset, depth)
    }

    override fun toString(): String {
        return "WIDE " + axisToString(axis) + " " + directionToString(direction) + " $offset $depth"
    }
}

// A gear move is like a basic move but the row/col after the selected one also moves in the
// opposite direction.
class GearMove(
    override val axis: Axis,
    override var direction: Direction,
    override val offset: Int
) : BasicMove(axis, direction, offset) {
    override fun updatePositions(progress: Double, board: Array<Array<GameCell>>) {
        if (axis == Axis.HORIZONTAL) {
            val numRows = board.size
            updatePositionsRow(direction, offset, progress, board)
            updatePositionsRow(opposite(direction), (offset + 1) % numRows, progress, board)
        } else {
            val numCols = board[0].size
            updatePositionsCol(direction, offset, progress, board)
            updatePositionsCol(opposite(direction), (offset + 1) % numCols, progress, board)
        }
    }

    override fun updateGrid(board: Array<Array<GameCell>>) {
        if (axis == Axis.HORIZONTAL) {
            val numRows = board.size
            updateGridRow(direction, offset, board)
            updateGridRow(opposite(direction), (offset + 1) % numRows, board)
        } else {
            val numCols = board[0].size
            updateGridCol(direction, offset, board)
            updateGridCol(opposite(direction), (offset + 1) % numCols, board)
        }
    }

    override fun inverse(): Move {
        return GearMove(axis, opposite(direction), offset)
    }

    override fun toString(): String {
        return "GEAR " + axisToString(axis) + " " + directionToString(direction) + " $offset"
    }
}

// A Carousel move forms a ring with the row/col that was selected and it's neighbor and does a
// circular shift.
open class CarouselMove(
    private val axis: Axis,
    private var direction: Direction,
    private val offset: Int
) : Move {
    override val isLegal = true

    override fun updatePositions(progress: Double, board: Array<Array<GameCell>>) {
        // Update the positions of relevant cells
        if (axis == Axis.HORIZONTAL) {
            updatePositionsRows(direction, offset, progress, board)
        } else {
            updatePositionsCols(direction, offset, progress, board)
        }
    }

    private fun updatePositionsRows(
        direction: Direction,
        offset: Int,
        progress: Double,
        board: Array<Array<GameCell>>
    ) {
        val numCols = board[0].size
        val numRows = board.size
        val rowRightIdx = if (direction == Direction.FORWARD) {
            offset
        } else {
            (offset + 1) % numRows
        }
        val rowLeftIdx = if (direction == Direction.FORWARD) {
            (offset + 1) % numRows
        } else {
            offset
        }

        for (col in 0 until numCols - 1) {
            board[rowRightIdx][col].offsetX = progress
        }
        board[rowRightIdx][numCols - 1].offsetY = (progress * direction.dir)

        for (col in 1 until numCols) {
            board[rowLeftIdx][col].offsetX = -1 * progress
        }
        board[rowLeftIdx][0].offsetY = (-1 * progress * direction.dir)
    }

    private fun updatePositionsCols(
        direction: Direction,
        offset: Int,
        progress: Double,
        board: Array<Array<GameCell>>
    ) {
        val numCols = board[0].size
        val numRows = board.size
        val colDownIdx = if (direction == Direction.FORWARD) {
            offset
        } else {
            (offset + 1) % numCols
        }
        val colUpIdx = if (direction == Direction.FORWARD) {
            (offset + 1) % numCols
        } else {
            offset
        }

        for (row in 0 until numRows - 1) {
            board[row][colDownIdx].offsetY = progress
        }
        board[numRows - 1][colDownIdx].offsetX = (progress * direction.dir)

        for (row in 1 until numRows) {
            board[row][colUpIdx].offsetY = -1 * progress
        }
        board[0][colUpIdx].offsetX = (-1 * progress * direction.dir)
    }

    override fun updateGrid(board: Array<Array<GameCell>>) {
        if (axis == Axis.HORIZONTAL) {
            updateGridRow(board)
        } else {
            updateGridCol(board)
        }
    }

    private fun updateGridRow(board: Array<Array<GameCell>>) {
        val numCols = board[0].size
        val numRows = board.size
        val rowRightIdx = if (direction == Direction.FORWARD) {
            offset
        } else {
            (offset + 1) % numRows
        }
        val rowLeftIdx = if (direction == Direction.FORWARD) {
            (offset + 1) % numRows
        } else {
            offset
        }
        val rightEnd = board[rowRightIdx][numCols - 1]
        val leftEnd = board[rowLeftIdx][0]

        for (col in (numCols - 1 downTo 1)) {
            board[rowRightIdx][col] = board[rowRightIdx][col - 1]
        }
        for (col in (0 until numCols - 1)) {
            board[rowLeftIdx][col] = board[rowLeftIdx][col + 1]
        }

        board[rowRightIdx][0] = leftEnd
        board[rowLeftIdx][numCols - 1] = rightEnd
    }

    private fun updateGridCol(board: Array<Array<GameCell>>) {
        val numCols = board[0].size
        val numRows = board.size
        val colDownIdx = if (direction == Direction.FORWARD) {
            offset
        } else {
            (offset + 1) % numCols
        }
        val colUpIdx = if (direction == Direction.FORWARD) {
            (offset + 1) % numCols
        } else {
            offset
        }
        val topEnd = board[0][colUpIdx]
        val bottomEnd = board[numRows - 1][colDownIdx]

        for (row in (numRows - 1 downTo 1)) {
            board[row][colDownIdx] = board[row - 1][colDownIdx]
        }
        for (row in (0 until numRows - 1)) {
            board[row][colUpIdx] = board[row + 1][colUpIdx]
        }

        board[numRows - 1][colUpIdx] = bottomEnd
        board[0][colDownIdx] = topEnd
    }

    override fun inverse(): Move {
        return CarouselMove(axis, opposite(direction), offset)
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

    // should never happen, these moves shouldn't get pushed to the undo stack
    override fun inverse(): Move {
        return this
    }

    // should never happen, these should never be serialized
    override fun toString(): String {
        return ""
    }
}

// Helper for loading saved moves from files.
fun stringToMove(s: String): Move? {
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
            BasicMove(axis, direction, offset)
        }
        "WIDE" -> {
            WideMove(axis, direction, offset, splits[4].toInt())
        }
        "GEAR" -> {
            GearMove(axis, direction, offset)
        }
        "CAROUSEL" -> {
            CarouselMove(axis, direction, offset)
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