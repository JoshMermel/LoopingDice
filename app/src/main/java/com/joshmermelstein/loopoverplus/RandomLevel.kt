package com.joshmermelstein.loopoverplus

import android.content.Context
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.random.Random

fun fromRandomMoveEffect(name: String, depth: Int?, axis: Axis): MoveEffect {
    return when (name) {
        "Gear" -> GearMoveEffect(axis)
        "Carousel" -> CarouselMoveEffect(axis)
        "Wide" -> WideMoveEffect(axis, depth!!)
        "Lightning" -> LightningMoveEffect(axis)
        "Bandaged" -> BandagedMoveEffect(axis)
        "Enabler" -> BasicMoveEffect(axis)
        "Arrows" -> BasicMoveEffect(axis)
        "Dynamic Bandaging" -> BasicMoveEffect(axis)
        "Static Cells" -> WideMoveEffect(axis, depth!!)
        else -> BasicMoveEffect(axis)
    }
}

fun fromRandomValidator(name: String, context: Context): MoveValidator {
    return when (name) {
        "Arrows" -> ArrowsValidator(context.getString(R.string.arrowValidatorHelptext))
        "Dynamic Bandaging" -> DynamicBandagingValidator(context.getString(R.string.dynamicValidatorHelptext))
        "Enabler" -> EnablerValidator(context.getString(R.string.enablerValidatorHelptext))
        "Static Cells" -> StaticCellsValidator(context.getString(R.string.staticValidatorHelptext))
        else -> MoveValidator("")
    }
}

// helper for replacing black square with gold in modes where black has a special meaning
fun blackToGold(i: Int): Int = if (i % 6 == 4) i + 1 else i

fun generateBicolorGoal(numRows: Int, numCols: Int): Array<Int> {
    val colors = (0..3).shuffled()
    return if (numCols % 2 == 0) {
        Array(numRows * numCols) { if (it % numCols < numCols / 2) colors[0] else colors[1] }
    } else {
        Array(numRows * numCols) { if (it < numRows * numCols / 2) colors[0] else colors[1] }
    }
}

fun sqrt(i: Int): Int {
    return floor(sqrt(i.toDouble())).toInt()
}

fun generateSpeckledGoal(numRows: Int, numCols: Int, indicesToAvoid: Set<Int>): List<Int> {
    val colors = (0..3).shuffled().toMutableList()
    val background = colors.removeFirst()
    val board = Array(numRows * numCols) { background }

    for (idx in (0 until numRows * numCols).filter { it !in indicesToAvoid }.shuffled()
        .take(sqrt(numRows * numCols))) {
        board[idx] = colors.random()
    }

    return board.toList()
}

// Slices a |numRows| by |numCols| size rectangle out of
// 0  1  2  3  4  5
// 6  7  ...
// ...
// 30         ... 35
fun generateUniqueGoal(numRows: Int, numCols: Int): Array<Int> {
    return (0..35).filter { (it < numRows * 6) && (it % 6 < numCols) }.toTypedArray()
}

// Slices a |numRows| by |numCols| size rectangle out of
// 0  1  2  3  4  5
// 0  1  2  ...
// ...
fun generateColumnsGoal(numRows: Int, numCols: Int): Array<Int> {
    return generateUniqueGoal(numRows, numCols).map { it % 6 }.toTypedArray()
}

// Generates a sensible goal for modes without special cells. Modes with special cells should
// overwrite those cells into the returned array.
fun generateBasicGoal(numRows: Int, numCols: Int, colorScheme: String): Array<Int> {
    return when (colorScheme) {
        "Bicolor" -> generateBicolorGoal(numRows, numCols)
        "Speckled" -> generateSpeckledGoal(numRows, numCols, emptySet()).toTypedArray()
        "Columns" -> generateColumnsGoal(numRows, numCols)
        else -> generateUniqueGoal(numRows, numCols)
    }
}

fun generateEnablerGoal(
    numRows: Int,
    numCols: Int,
    colorScheme: String,
    numEnablers: String
): Array<String> {
    val enablersIndices = when (numEnablers) {
        "Common" -> (1 until numRows * numCols).shuffled().take(numRows * numCols / 8)
        "Frequent" -> (1 until numRows * numCols).shuffled().take(numRows * numCols / 4)
        else -> listOf(0)
    }.toSet()

    // In Speckled + Enabler, we avoid putting speckles in the same places as enabler cells
    val goal = when (colorScheme) {
        "Speckled" -> generateSpeckledGoal(numRows, numCols, enablersIndices)
        else -> generateBasicGoal(numRows, numCols, colorScheme).toList()
    }.map { it.toString() }.toTypedArray()


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
    val bandagedCount = when (numBandaged) {
        "Rare" -> (numRows * numCols / 6) + 1
        "Common" -> numRows * numCols / 2
        "Frequent" -> 2 * numRows * numCols / 3
        else -> 1
    }
    val fixedIndices = (1 until numRows * numCols).shuffled().take(bandagedCount)

    val board = when (colorScheme) {
        "Bicolor", "Speckled" -> {
            // In both Bicolor and Speckled, non-fixed cells are all one color. We handle that by
            // starting with a monocolor board and overwriting random cells with fixed cells.
            val color = Random.nextInt(0, 4).toString()
            List(numRows * numCols) { color }
        }
        else -> generateBasicGoal(numRows, numCols, colorScheme).map { blackToGold(it) }
    }.map { it.toString() }.toTypedArray()

    // In Speckled and Unique, we want to very the number of pips. In other mode we always want 1 pip.
    val modulus = when (colorScheme) {
        "Speckled", "Unique" -> 6
        else -> 1
    }

    return addFixedCells(board, fixedIndices, modulus)
}

fun randomAxisPrefix(): String {
    return if (Random.nextBoolean()) "H " else "V "
}

fun addArrowCells(board: Array<Int>, indices: Set<Int>): Array<String> {
    return board.mapIndexed { idx, i ->
        (if (idx in indices) {
            randomAxisPrefix() + (i % 6).toString()
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
    val arrowsIndices = (1 until numRows * numCols).shuffled().take(arrowsCount).toSet()

    return addArrowCells(
        generateBasicGoal(numRows, numCols, colorScheme),
        arrowsIndices
    )
}

fun addBolts(board: Array<Int>, indices: List<Int>): Array<String> {
    return board.mapIndexed { idx, i ->
        (if (idx in indices) {
            "B " + (i % 6).toString()
        } else {
            i.toString()
        })
    }.toTypedArray()
}

fun generateLightningGoal(
    numRows: Int,
    numCols: Int,
    colorScheme: String,
    numBolts: String
): Array<String> {
    val boltCount = 1 + when (numBolts) {
        "Rare" -> 1
        "Common" -> numRows * numCols / 8
        "Frequent" -> numRows * numCols / 4
        else -> 0
    }
    val boltIndices = (1 until numRows * numCols).shuffled().take(boltCount)

    return addBolts(
        generateBasicGoal(numRows, numCols, colorScheme),
        boltIndices
    )
}

fun generateStaticCellGoal(numRows: Int, numCols: Int, colorScheme: String): Array<String> {
    // Speckled Static is special because we want to avoid putting a speckle where we've
    // put the fixed cell.
    val ret = when (colorScheme) {
        "Speckled" -> generateSpeckledGoal(numRows, numCols, setOf(0))
        else -> generateBasicGoal(numRows, numCols, colorScheme).map { blackToGold(it) }
    }.map { it.toString() }.toTypedArray()

    ret[0] = "F 0"
    return ret
}

fun generateBandagedGoal(
    numRows: Int,
    numCols: Int,
    colorScheme: String,
    numBlocks: String
): Array<String> {
    if (colorScheme != "Speckled") {
        val goal = generateBasicGoal(numRows, numCols, colorScheme).map { blackToGold(it) }
        return addBonds(numRows, numCols, goal, numBlocks).toTypedArray()
    } else {
        // Speckled bandaged is special because we avoid placing speckles on bandaged blocks
        val colors = (0..3).shuffled().toMutableList()
        val background = colors.removeFirst()
        val board = addBonds(
            numRows,
            numCols,
            List(numRows * numCols) { background },
            numBlocks
        ).toTypedArray()

        // Compute which locations shouldn't get a speckle
        val bondedIndices: Set<Int> =
            board.mapIndexed { idx, s -> if (s.contains("[UDLR]".toRegex())) idx else null }
                .filterNotNull()
                .toSet()

        // Add speckles to other places
        for (idx in (0 until numRows * numCols).filter { it !in bondedIndices }.shuffled()
            .take(sqrt(numRows * numCols))) {
            board[idx] = colors.random().toString()
        }
        return board
    }
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
    ).flatten().toSet().filter { move -> move !is IllegalMove }.run {
        if (this.isEmpty()) {
            IllegalMove()
        } else {
            this.random()
        }
    }
}

fun scramble(
    solved: Array<String>,
    factory: MoveFactory,
    num_rows: Int,
    num_cols: Int,
    data: GameCellMetadata
): Array<String> {
    val gameBoard = GameBoard(num_rows, num_cols, solved, data)
    repeat(2001) {
        randomMove(gameBoard, factory).finalize(gameBoard)
    }
    return gameBoard.toString().split(",").toTypedArray()
}

// Initial and final are optional params for when the caller knows what they want the initial and final state to be.
fun generateRandomLevel(
    options: RandomLevelParams,
    initial: Array<String>?,
    goal: Array<String>?,
    context: Context
): GameplayParams {
    val rowEffect =
        fromRandomMoveEffect(options.rowMode, options.rowDepth, Axis.HORIZONTAL)
    val colEffect =
        fromRandomMoveEffect(options.colMode ?: options.rowMode, options.colDepth, Axis.VERTICAL)
    val validator = fromRandomValidator(options.rowMode, context)
    val factory = MoveFactory(rowEffect, colEffect, validator)

    if (initial != null && goal != null) {
        return GameplayParams(
            "∞$options",
            options.numRows,
            options.numCols,
            factory,
            initial,
            goal,
            ""
        )
    }

    val randomGoal: Array<String> = when (options.rowMode) {
        "Enabler" -> generateEnablerGoal(
            options.numRows,
            options.numCols,
            options.colorScheme,
            options.density!!
        )
        "Dynamic Bandaging" -> generateDynamicBandagingGoal(
            options.numRows,
            options.numCols,
            options.colorScheme,
            options.density!!
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
            options.density!!
        )
        "Lightning" -> generateLightningGoal(
            options.numRows,
            options.numCols,
            options.colorScheme,
            options.density!!
        )
        "Bandaged" -> generateBandagedGoal(
            options.numRows,
            options.numCols,
            options.colorScheme,
            options.density!!
        )
        else -> generateBasicGoal(
            options.numRows,
            options.numCols,
            options.colorScheme
        ).map { it.toString() }.toTypedArray()
    }
    val randomStart =
        scramble(randomGoal, factory, options.numRows, options.numCols, fakeGameCellMetadata())

    return GameplayParams(
        "∞$options",
        options.numRows,
        options.numCols,
        factory,
        randomStart,
        randomGoal,
        ""
    )
}