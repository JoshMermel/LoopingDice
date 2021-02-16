package com.joshmermelstein.loopoverplus

import java.util.*

// A queue of moves along with info about when they should be executed.
class MoveQueue {
    // TODO(jmerm): is there a better type for this?
    private val moves = mutableListOf<MoveAndTime>()
    private var lastMoveTime: Long = 0
    private val moveDuration: Long = 200 * 1000 * 1000

    // Iterates through the queue of moves and executes those whose time has come. Also clears moves
    // that have completed.
    fun runMoves(currentTime: Long, manager: GameManager) {
        for (moveAndTime in this.moves) {
            moveAndTime.move.run(manager, moveAndTime.startTime, moveAndTime.endTime, currentTime)
        }
        moves.filter { m -> m.endTime < currentTime }.forEach { moves.remove(it) }
    }

    // Adds a move to the queue.
    fun addMove(move: Move) {
        val startTime = if (moves.isEmpty()) {
            System.nanoTime()
        } else {
            this.lastMoveTime
        }
        val endTime = startTime + moveDuration
        moves.add(MoveAndTime(move, startTime, endTime))
        this.lastMoveTime = endTime
    }

    // Clears the queue.
    fun reset() {
        moves.clear()
    }
}

// Struct for bundling a move with its start/end time
class MoveAndTime(var move: Move, var startTime: Long, var endTime: Long)