package com.joshmermelstein.loopoverplus

import android.content.Context
import kotlin.random.Random

fun fromRandomFactory(name: String, rowDepth: Int?, colDepth: Int?): MoveFactory {
    return when (name) {
        "Gear" -> GearMoveFactory()
        "Carousel" -> CarouselMoveFactory()
        "Wide" -> WideMoveFactory(rowDepth!!, colDepth!!)
        "Enabler" -> EnablerMoveFactory()
        "Dynamic Bandaging" -> DynamicBandagingMoveFactory()
        "Static Cells" -> StaticCellsMoveFactory(rowDepth!!, colDepth!!)
        "Arrows" -> AxisLockedMoveFactory()
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

// TODO(jmerm): more principled approach to scrambling
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

fun generateBicolorGoal(numRows: Int, numCols: Int): Array<String> {
    val colors = (1..4).shuffled().take(2).map { i -> i.toString() }
    return if (numCols % 2 == 0) {
        Array(numRows * numCols) { i ->
            if (i % numCols < numCols / 2) {
                colors[0]
            } else {
                colors[1]
            }
        }
    } else {
        Array(numRows * numCols) { i ->
            if (i < numRows * numCols / 2) {
                colors[0]
            } else {
                colors[1]
            }
        }
    }
}

fun generateBasicGoal(numRows: Int, numCols: Int, colorScheme: String): Array<String> {
    return when (colorScheme) {
        "Bicolor" -> {
            // TODO(jmerm): more interesting patterns? Staircase?
            generateBicolorGoal(numRows, numCols)
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

fun generateEnablerGoal(
    numRows: Int,
    numCols: Int,
    colorScheme: String,
    numEnablers: String
): Array<String> {
    val goal = generateBasicGoal(numRows, numCols, colorScheme)
    when (numEnablers) {
        "Rare" -> {
            goal[0] = "E"
        }
        "Common" -> {
            for (idx in (1 until numRows * numCols).shuffled().take(1 + (numRows * numCols / 8))) {
                goal[idx] = "E"
            }
        }
        "Frequent" -> {
            for (idx in (1 until numRows * numCols).shuffled().take(1 + (numRows * numCols / 4))) {
                goal[idx] = "E"
            }
        }
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
            // TODO(jmerm): consider if some of these lambdas in maps could be named functions for clarity
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

fun randomAxis() : String {
    return if (Random.nextBoolean()) "H " else "V "
}

fun generateArrowsGoal(
    numRows: Int,
    numCols: Int,
    colorScheme: String,
    numArrows: String
): Array<String> {
    // TODO(jmerm): think more about these densities
    val arrowsCount = when (numArrows) {
        "Rare" -> (numRows * numCols / 6) + 1
        "Common" -> numRows * numCols / 4
        "Frequent" -> numRows * numCols / 2
        else -> 1
    }
    val arrowsIdxs = (1 until numRows * numCols).shuffled().take(arrowsCount)

    return when (colorScheme) {
        // TODO(jmerm): color scheme
        "Bicolor" -> {
            val ret = generateBasicGoal(numRows, numCols, colorScheme)
            for (idx in arrowsIdxs) {
                ret[idx] = randomAxis() + ret[idx]
            }
            ret
        }
        "Columns" -> {
            val ret = (0..35).filter { (it < numRows * 6) && (it % 6 < numCols) }.toTypedArray()
                .mapIndexed { idx, i ->
                    (if (idx in arrowsIdxs) {
                        randomAxis() + (i % 6 + 1).toString()
                    } else {
                        (i % 6 + 1).toString()
                    })
                }.toTypedArray()
            ret
        }
        else -> { // unique
            val ret = (0..35).filter { (it < numRows * 6) && (it % 6 < numCols) }
                .mapIndexed { idx, i ->
                    (if (idx in arrowsIdxs) {
                        randomAxis() + (i % 6 + 1).toString()
                    } else {
                        (i + 1).toString()
                    })
                }.toTypedArray()
            ret
        }
    }
}

// TODO(jmerm): add more options here
fun generateStaticCellGoal(numRows: Int, numCols: Int, colorScheme: String): Array<String> {
    return when (colorScheme) {
        "Bicolor" -> {
            val ret = generateBasicGoal(numRows, numCols, colorScheme)
            ret[0] = "F 0"
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
            ret[0] = "F 0"
            ret
        }
        else -> { // unique
            val ret = (0..35).filter { (it < numRows * 6) && (it % 6 < numCols) }
                .map { i ->
                    (if (i % 6 == 4) {
                        i + 2
                    } else {
                        i + 1
                    }).toString()
                }.toTypedArray()
            ret[0] = "F 0"
            ret
        }
    }
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
            generateEnablerGoal(
                options.numRows,
                options.numCols,
                options.colorScheme,
                options.numEnablers!!
            )
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
        "Arrows" -> {
            generateArrowsGoal(
                options.numRows,
                options.numCols,
                options.colorScheme,
                options.numArrows!!
            )
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