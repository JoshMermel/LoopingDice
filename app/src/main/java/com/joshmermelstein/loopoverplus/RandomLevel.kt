package com.joshmermelstein.loopoverplus

import android.content.Context
import android.util.Log
import kotlin.random.Random

// TODO(jmerm): tests for things in this file

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

// helper for applying a "Columns" or "Unique" to a boardTemplate
fun toId(i: Int, mode: String) = if (mode == "Columns") i % 6 + 1 else i + 1

// helper for replacing black square with gold in modes where black has a special meaning
fun blackToGold(i: Int): Int = if (i % 6 == 5) i + 1 else i

// TODO(jmerm): more interesting patterns? Staircase?
fun generateBicolorGoal(numRows: Int, numCols: Int): Array<Int> {
    val colors = (1..4).shuffled()
    return if (numCols % 2 == 0) {
        Array(numRows * numCols) { if (it % numCols < numCols / 2) colors[0] else colors[1] }
    } else {
        Array(numRows * numCols) { if (it < numRows * numCols / 2) colors[0] else colors[1] }
    }
}

// Generates a sensible goal for modes without special cells. Modes with special cells should
// overwrite those cells into the returned array.
fun generateBasicGoal(numRows: Int, numCols: Int, colorScheme: String): Array<Int> {
    return if (colorScheme == "Bicolor") {
        generateBicolorGoal(numRows, numCols)
    } else {
        // Slices a |numRows| by |numCols| size rectangle out of
        // 1  2  3  4  5  6
        // 7  8  ...
        // ...
        // 31         ... 36
        // and possibly modifies it so all dice have 1 pip.
        (0..35).filter { (it < numRows * 6) && (it % 6 < numCols) }.map { toId(it, colorScheme) }
            .toTypedArray()
    }
}

fun generateEnablerGoal(
    numRows: Int,
    numCols: Int,
    colorScheme: String,
    numEnablers: String
): Array<String> {
    val goal = generateBasicGoal(numRows, numCols, colorScheme).map { it.toString() }.toTypedArray()
    val enablersIndices = when (numEnablers) {
        "Common" -> (1 until numRows * numCols).shuffled().take(numRows * numCols / 8)
        "Frequent" -> (1 until numRows * numCols).shuffled().take(numRows * numCols / 4)
        else -> listOf(0)
    }
    for (idx in enablersIndices) {
        goal[idx] = "E"
    }

    return goal
}

fun addFixedCells(board: Array<String>, indices: List<Int>, modulus: Int): Array<String> {
    var pips = 0
    for (idx in indices) {
        board[idx] = "F " + (pips + 1).toString()
        pips = (pips + 1) % modulus
    }
    return board
}

fun generateDynamicBandagingGoal(
    numRows: Int,
    numCols: Int,
    colorScheme: String,
    numBandaged: String
): Array<String> {
    // Bicolor Dynamic is unusual since the black squares are the second color. We handle that by
    // starting with a monocolor board and overwriting random cells with fixed cells.
    val board = if (colorScheme == "Bicolor") {
        val color = Random.nextInt(1, 5).toString()
        Array(numRows * numCols) { color }
    } else {
        generateBasicGoal(numRows, numCols, colorScheme).map { blackToGold(it) }
            .map { it.toString() }.toTypedArray()
    }

    val bandagedCount = when (numBandaged) {
        "Rare" -> (numRows * numCols / 6) + 1
        "Common" -> numRows * numCols / 2
        "Frequent" -> 2 * numRows * numCols / 3
        else -> 1
    }
    val fixedIndices = (1 until numRows * numCols).shuffled().take(bandagedCount)
    val modulus = if (colorScheme == "Unique") 6 else 1
    return addFixedCells(board, fixedIndices, modulus)

}

fun randomAxisPrefix(): String {
    return if (Random.nextBoolean()) "H " else "V "
}

fun addArrowCells(board: Array<Int>, indices: List<Int>, colorScheme: String): Array<String> {
    return board.mapIndexed { idx, i ->
        (if (idx in indices) {
            // This expressions is like `i%6` except 0's are replaced by 6s (since 0 is not a valid ColorId)
            randomAxisPrefix() + (mod(i - 1, 6) + 1).toString()
        } else {
            i.toString()
        })
    }.toTypedArray()
}

fun generateArrowsGoal(
    numRows: Int,
    numCols: Int,
    colorScheme: String,
    numArrows: String
): Array<String> {
    val arrowsCount = 1 + when (numArrows) {
        "Rare" -> numRows * numCols / 6
        "Common" -> numRows * numCols / 4
        "Frequent" -> numRows * numCols / 3
        else -> 0
    }
    val arrowsIndices = (1 until numRows * numCols).shuffled().take(arrowsCount)

    return addArrowCells(
        generateBasicGoal(numRows, numCols, colorScheme),
        arrowsIndices,
        colorScheme
    )
}

fun generateStaticCellGoal(numRows: Int, numCols: Int, colorScheme: String): Array<String> {
    val ret = generateBasicGoal(numRows, numCols, colorScheme).map { blackToGold(it) }
        .map { it.toString() }.toTypedArray()
    ret[0] = "F 0"
    return ret
}

fun randomMove(board: GameBoard, factory: MoveFactory): Move {
    return listOf(
        (0 until board.numRows).map {
            factory.makeMove(Axis.HORIZONTAL, Direction.FORWARD, it, board)
        },
        (0 until board.numRows).map {
            factory.makeMove(Axis.HORIZONTAL, Direction.BACKWARD, it, board)
        },
        (0 until board.numCols).map {
            factory.makeMove(Axis.VERTICAL, Direction.FORWARD, it, board)
        },
        (0 until board.numCols).map {
            factory.makeMove(Axis.VERTICAL, Direction.BACKWARD, it, board)
        },
    ).flatten().filter { move -> move !is IllegalMove }.run {
        if (this.isEmpty()) {
            IllegalMove(emptyList())
        } else {
            this[Random.nextInt(this.size)]
        }
    }
}

fun scramble(
    solved: Array<String>, factory: MoveFactory, num_rows: Int, num_cols: Int, context: Context
): Array<String> {
    val gameBoard = GameBoard(num_rows, num_cols, solved, context)
    for (i in (0..2000)) {
        randomMove(gameBoard, factory).finalize(gameBoard)
    }
    return gameBoard.toString().split(",").toTypedArray()
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
    val goal: Array<String> = when (options.rowMode) {
        "Enabler" -> generateEnablerGoal(
            options.numRows,
            options.numCols,
            options.colorScheme,
            options.numEnablers!!
        )
        "Dynamic Bandaging" -> generateDynamicBandagingGoal(
            options.numRows,
            options.numCols,
            options.colorScheme,
            options.numBandaged!!
        )
        "Static Cells" -> generateStaticCellGoal(
            options.numRows,
            options.numCols,
            options.colorScheme
        )
        "Arrows" -> generateArrowsGoal(
            options.numRows,
            options.numCols,
            options.colorScheme,
            options.numArrows!!
        )
        else -> generateBasicGoal(
            options.numRows,
            options.numCols,
            options.colorScheme
        ).map { it.toString() }.toTypedArray()
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