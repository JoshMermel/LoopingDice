package com.joshmermelstein.loopoverplus

import kotlin.math.pow

// This file holds the Move interface as well as several sub-interfaces that hold shared utilities
// for common needs of Move subclasses.

// A Move represents the outcome of the user swiping on the board. Most Moves slide pieces around
// the board in an implementation-defined way but this is not required (e.g. see IllegalMove)
interface Move {
    fun run(board: GameBoard, startTime: Long, endTime: Long, currentTime: Long) {
        // Compute how much of the move has happened
        val progress: Double = when {
            (currentTime < startTime) -> {
                // It's too soon to do anything.
                return
            }
            (currentTime < endTime) -> {
                // We're in the middle of a move.
                ease((currentTime - startTime) / (endTime - startTime).toDouble())
            }
            else -> {
                // The move is over, snap to 100% progress.
                1.0
            }
        }

        // Update the positions of relevant cells
        animateProgress(progress, board)

        // If it's over, also finalize
        if (currentTime > endTime) {
            finalize(board)
        }
    }

    // https://easings.net/#easeOutBack
    fun ease(x: Double): Double {
        val c1 = 1.70158
        val c3 = c1 + 1
        return 1 + c3 * (x - 1).pow(3) + c1 * (x - 1).pow(2)
    }

    // Updates board based on |progress| to animate the move for the user.
    // Progress will range from 0 (before the move starts) to 1 (after the move completes).
    fun animateProgress(progress: Double, board: GameBoard)

    // Do any work that needs to happen after a move completes but before the next move can begin.
    fun finalize(board: GameBoard)
}

// A transition represents the action of a single cell during a Move. Coordinates may be out of
// range to indicate a cell sliding off screen.
class Transition(
    val x0: Int,
    val y0: Int,
    val x1: Int,
    val y1: Int
) {
    override fun toString(): String {
        return "($y0, $x0)->($y1, $x1)"
    }
}

// Subclasses of LegalMove get counted toward move counts, are written to the undo stack, and are
// saved when the user closes the level
// LegalMove also provides shared logic to make further subclasses easier to implement. Rather than
// Move subclasses implementing the same logic for updating draw positions and updating the
// underlying grid afterward, this class helps them implement both in terms of a list of Transitions.
interface LegalMove : Move {
    val axis: Axis
    val direction: Direction
    val offset: Int
    val transitions: MutableList<Transition>

    override fun animateProgress(progress: Double, board: GameBoard) {
        for (t in transitions) {
            val cell = board.getCell(t.y0, t.x0)
            cell.offsetX = (t.x1 - t.x0) * progress
            cell.offsetY = (t.y1 - t.y0) * progress
        }
    }

    override fun finalize(board: GameBoard) {
        // Read all updates into a map so they can be executed without overwriting each other.
        val updates: MutableMap<Transition, GameCell> = mutableMapOf()
        for (t in transitions) {
            updates[t] = board.getCell(t.y0, t.x0)
        }

        // Write those updates back into the board.
        for ((t, cell) in updates) {
            cell.finalize(board.numRows, board.numCols)
            board.setCell(t.y1, t.x1, cell)
        }
    }

    // Returns a move that undoes this one, used for managing undo and redo stacks.
    fun inverse(): LegalMove

    // Used for saving move history to a file.
    override fun toString(): String

    // Human readable string of this move
    fun toUserString(): String {
        return when (axis) {
            Axis.HORIZONTAL -> "Row"
            Axis.VERTICAL -> "Col"
        } + "$offset" + when (direction) {
            Direction.FORWARD -> ""
            Direction.BACKWARD -> "'"
        }
    }
}

// Yet another helper for implementing shared logic and make other moves easier to implement.
// Helpers are provided for basic looping moves on rows and columns.
interface RowColMove : LegalMove {
    fun addHorizontal(direction: Direction, offset: Int, numCols: Int) {
        val delta = if (direction == Direction.FORWARD) 1 else -1
        for (col in 0 until numCols) {
            transitions.add(Transition(col, offset, col + delta, offset))
        }
    }

    fun addVertical(direction: Direction, offset: Int, numRows: Int) {
        val delta = if (direction == Direction.FORWARD) 1 else -1
        for (row in 0 until numRows) {
            transitions.add(Transition(offset, row, offset, row + delta))
        }
    }
}

// Helper for loading saved moves from files.
fun stringToMove(s: String, numRows: Int, numCols: Int): LegalMove? {
    val splits = s.split(" ")
    if (splits.size < 4 || !isNumeric(splits[3])) {
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
        "BASIC" -> BasicMove(axis, direction, offset, numRows, numCols)
        "WIDE" -> {
            if (isNumeric(splits[4])) {
                WideMove(axis, direction, offset, numRows, numCols, splits[4].toInt())
            } else {
                null
            }
        }
        "GEAR" -> GearMove(axis, direction, offset, numRows, numCols)
        "CAROUSEL" -> CarouselMove(axis, direction, offset, numRows, numCols)
        else -> null
    }
}

enum class Axis(val id: String) {
    VERTICAL("V"),
    HORIZONTAL("H");

    override fun toString(): String {
        return id
    }
}

enum class Direction(val id: String) {
    FORWARD("F"),
    BACKWARD("B");

    override fun toString(): String {
        return id
    }
}
