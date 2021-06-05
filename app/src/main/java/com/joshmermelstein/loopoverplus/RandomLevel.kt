package com.joshmermelstein.loopoverplus

import android.content.Context
import android.util.Log
import kotlin.random.Random

fun fromRandomFactory(name: String, rowDepth: Int?, colDepth: Int?): MoveFactory {
    return when (name) {
        "Gear" -> GearMoveFactory()
        "Carousel" -> CarouselMoveFactory()
        "Wide" -> WideMoveFactory(rowDepth!!, colDepth!!)
        "Enabler" -> EnablerMoveFactory()
        "Dynamic Bandaging" -> DynamicBandagingMoveFactory()
        "Static Cells" -> StaticCellsMoveFactory(rowDepth!!, colDepth!!)
        else -> BasicMoveFactory()
    }
}

fun randomMove(
    board: GameBoard,
    factory: MoveFactory,
    num_rows: Int,
    num_cols: Int
): Move {
    val direction = if (Random.nextBoolean()) {
        Direction.FORWARD
    } else {
        Direction.BACKWARD
    }
    val seed = Random.nextInt(num_rows + num_cols)
    return if (seed < num_rows) {
        factory.makeMove(Axis.HORIZONTAL, direction, seed, board)
    } else {
        factory.makeMove(Axis.VERTICAL, direction, seed - num_rows, board)
    }
}

fun scramble(
    solved: Array<String>, factory: MoveFactory, num_rows: Int, num_cols: Int, context: Context
): Array<String> {
    val gameBoard = GameBoard(num_rows, num_cols, solved, context)
    for (i in (0..1000)) {
        val move = randomMove(gameBoard, factory, num_rows, num_cols)
        move.finalize(gameBoard)
    }
    return gameBoard.toString().split(",").toTypedArray()
}

fun generateBasicGoal(numRows: Int, numCols: Int, colorScheme: String): Array<String> {
    return when (colorScheme) {
        "Bicolor" -> {
            // TODO(jmerm): more interesting patterns? Staircase?
            Array<String>(numRows * numCols) { i ->
                if (i < numRows * numCols / 2) {
                    "1"
                } else {
                    "2"
                }
            }
        }
        "Columns" -> {
            // vertical stripes
            (0..35).filter { (it < numRows * 6) && (it % 6 < numCols) }
                .map { i -> ((i % 6) + 1).toString() }.toTypedArray()
        }
        else -> {
            // unique vertical stripes
            (0..35).filter { (it < numRows * 6) && (it % 6 < numCols) }
                .map { i -> (i + 1).toString() }.toTypedArray()
        }
    }
}

// TODO(jmerm): make this more configurable
fun generateEnablerGoal(numRows: Int, numCols: Int, colorScheme: String): Array<String> {
    val goal = generateBasicGoal(numRows, numCols, colorScheme)
    val numEnablers = Random.nextInt(1, numRows * numCols / 3)
    for (i in (0..numEnablers)) {
        goal[Random.nextInt(0, numRows * numCols)] = "E"
    }
    return goal
}

fun generateDynamicBandagingGoal(
    numRows: Int,
    numCols: Int,
    colorScheme: String,
    numBandaged: String
): Array<String> {
    val bandagedCount = when (numBandaged) {
        "Rare" -> (numRows * numCols / 6) + 1
        "Common" -> numRows * numCols / 2
        "Frequent" -> 2 * numRows * numCols / 3
        else -> 1
    }
    return when (colorScheme) {
        "Bicolor" -> {
            val ret = Array<String>(numRows * numCols) { _ -> "1" }
            for (idx in (1 until numRows * numCols).shuffled().take(bandagedCount)) {
                ret[idx] = "F 1"
            }
            ret
        }
        "Columns" -> {
            val ret = (0..35).filter { (it < numRows * 6) && (it % 6 < numCols) }
                .map { i ->
                    (if (i % 6 == 4) {
                        6
                    } else {
                        i % 6 + 1
                    }).toString()
                }.toTypedArray()
            for (idx in (1 until numRows * numCols).shuffled().take(bandagedCount)) {
                ret[idx] = "F 1"
            }
            ret
        }
        else -> { // unique
            var pips = 0
            val ret = (0..35).filter { (it < numRows * 6) && (it % 6 < numCols) }
                .map { i ->
                    (if (i % 6 == 4) {
                        i + 2
                    } else {
                        i + 1
                    }).toString()
                }.toTypedArray()
            for (idx in (1 until numRows * numCols).shuffled().take(bandagedCount)) {
                ret[idx] = "F " + (pips + 1).toString()
                pips = (pips + 1) % 6
            }
            ret
        }
    }
}

// TODO(jmerm): add more options here
fun generateStaticCellGoal(numRows: Int, numCols: Int, colorScheme: String): Array<String> {
    val goal = generateBasicGoal(numRows, numCols, colorScheme)
    goal[0] = "F 0"
    return goal
}

// TODO(jmerm): needing to take the context here is silly, fix that.
fun generateRandomLevel(options: RandomLevelParams, context: Context): GameplayParams {
    val factory: MoveFactory =
        if (options.rowMode == options.colMode || options.colMode == null) {
            fromRandomFactory(options.rowMode, options.rowDepth, options.colDepth)
        } else {
            CombinedMoveFactory(
                fromRandomFactory(options.rowMode, options.rowDepth, options.rowDepth),
                fromRandomFactory(options.colMode, options.colDepth, options.colDepth)
            )
        }
    val goal = when (options.rowMode) {
        "Enabler" -> {
            generateEnablerGoal(options.numRows, options.numCols, options.colorScheme)
        }
        "Dynamic Bandaging" -> {
            generateDynamicBandagingGoal(
                options.numRows,
                options.numCols,
                options.colorScheme,
                options.numBandaged!!
            )
        }
        "Static Cells" -> {
            generateStaticCellGoal(options.numRows, options.numCols, options.colorScheme)
        }
        else -> {
            generateBasicGoal(options.numRows, options.numCols, options.colorScheme)
        }
    }

    val start = scramble(goal, factory, options.numRows, options.numCols, context)

    return GameplayParams(
        "∞",
        options.numRows,
        options.numCols,
        factory,
        start,
        goal,
        ""
    )
}