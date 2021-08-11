package com.joshmermelstein.loopoverplus

import android.util.Log

/*
 * Generating random bandaging schemes is such a pain that I'm moving it to its own file to keep
 * everything else organized.
 *
 * Rather than thinking about each bond as an isolated problem, I instead think about placing
 * "blocks", which I define as "as set of cells that are connected by all sensible Bonds.
 *
 * The general outline of adding bond signatures to a board is:
 * 1. Figure out which blocks should be placed by looking it it up in a hardcoded map
 * 2. Arrange those blocks in a grid without overlap (see subclasses of BlockPlacer)
 * 3. Modify strings in the board (which was generated elsewhere) so that appropriate bonds are
 *    added to each cell.
 *
 * This system isn't perfect, there are all sorts of interesting bandaging schemes it cannot ever
 * generate. I think it's way too hard to tell whether boards are interesting to solvers or able to
 * be scrambled effectively so this approach lets me get around those problems. Due to the random
 * way bonds are placed, there is still a decent bit of variety in the bandaging schemes it
 * generates
 */

// A BondGrid represents the places where Bonds could be placed on a board.
// Empty strings represent cells with no bonds
// Strings containing any of {"U", "D", "L", "R"} represent cells with bonds in those directions.
typealias BondGrid = Array<Array<String>>

// Glorified Pair<Int, Int>.
data class BoardSize(val numRows: Int, val numCols: Int)

// Another glorified struct, for "how many of each kind of block should be added?"
class BondSignature(
    val numHDomino: Int,
    val numVDomino: Int,
    val numSquare: Int,
    val numHTriple: Int,
    val numVTriple: Int
) {
    // To save work, maps only care about cases where numRows >= numCols. transposing BondSignatures
    // lets us handle the remaining cases.
    fun transpose(): BondSignature {
        return BondSignature(numVDomino, numHDomino, numSquare, numVTriple, numHTriple)
    }
}

// TODO(jmerm): map to a list that we choose randomly from for variety?
// per-density maps from BoardSize(numRows, numCols) to a sensible bond signature for a board of those dimensions
val rareSignatures: Map<BoardSize, BondSignature> = mapOf(
    BoardSize(2, 2) to BondSignature(0, 1, 0, 0, 0),
    BoardSize(3, 2) to BondSignature(0, 1, 0, 0, 0),
    BoardSize(3, 3) to BondSignature(1, 0, 0, 0, 0),
    BoardSize(4, 2) to BondSignature(0, 1, 0, 0, 0),
    BoardSize(4, 3) to BondSignature(2, 0, 0, 0, 0),
    BoardSize(4, 4) to BondSignature(1, 1, 0, 0, 0),
    BoardSize(5, 2) to BondSignature(0, 2, 0, 0, 0),
    BoardSize(5, 3) to BondSignature(1, 1, 0, 0, 1),
    BoardSize(5, 4) to BondSignature(2, 0, 0, 0, 1),
    BoardSize(5, 5) to BondSignature(1, 0, 0, 1, 1),
    BoardSize(6, 2) to BondSignature(0, 1, 0, 0, 1),
    BoardSize(6, 3) to BondSignature(1, 1, 0, 0, 1),
    BoardSize(6, 4) to BondSignature(1, 0, 1, 0, 1),
    BoardSize(6, 5) to BondSignature(2, 2, 0, 0, 1),
)
val commonSignatures: Map<BoardSize, BondSignature> = mapOf(
    BoardSize(2, 2) to BondSignature(1, 0, 0, 0, 0),
    BoardSize(3, 2) to BondSignature(0, 1, 0, 0, 0),
    BoardSize(3, 3) to BondSignature(0, 0, 1, 0, 0),
    BoardSize(4, 2) to BondSignature(0, 1, 0, 0, 0),
    BoardSize(4, 3) to BondSignature(1, 1, 0, 0, 0),
    BoardSize(4, 4) to BondSignature(1, 1, 0, 1, 0),
    BoardSize(5, 2) to BondSignature(0, 1, 0, 0, 1),
    BoardSize(5, 3) to BondSignature(1, 0, 1, 0, 1),
    BoardSize(5, 4) to BondSignature(2, 1, 1, 0, 0),
    BoardSize(5, 5) to BondSignature(2, 1, 0, 1, 1),
    BoardSize(6, 2) to BondSignature(0, 2, 0, 0, 1),
    BoardSize(6, 3) to BondSignature(1, 1, 1, 0, 1),
    BoardSize(6, 4) to BondSignature(1, 0, 1, 1, 1),
    BoardSize(6, 5) to BondSignature(1, 1, 1, 1, 1),
)
val frequentSignatures: Map<BoardSize, BondSignature> = mapOf(
    BoardSize(2, 2) to BondSignature(2, 0, 0, 0, 0),
    BoardSize(3, 2) to BondSignature(0, 2, 0, 0, 0),
    BoardSize(3, 3) to BondSignature(1, 1, 0, 0, 0),
    BoardSize(4, 2) to BondSignature(0, 2, 0, 0, 0),
    BoardSize(4, 3) to BondSignature(0, 1, 1, 0, 0),
    BoardSize(4, 4) to BondSignature(0, 0, 2, 0, 0),
    BoardSize(5, 2) to BondSignature(0, 2, 0, 0, 1),
    BoardSize(5, 3) to BondSignature(0, 1, 1, 0, 1),
    BoardSize(5, 4) to BondSignature(1, 2, 1, 1, 0),
    BoardSize(5, 5) to BondSignature(1, 2, 1, 1, 0),
    BoardSize(6, 2) to BondSignature(0, 1, 0, 0, 2),
    BoardSize(6, 3) to BondSignature(0, 2, 2, 0, 0),
    BoardSize(6, 4) to BondSignature(1, 1, 1, 1, 1),
    BoardSize(6, 5) to BondSignature(2, 2, 1, 1, 1),
)

// Ultimate map for figuring out which BondSignature to apply to a board.
val allSignaturesMap = mapOf(
    "Rare" to rareSignatures,
    "Common" to commonSignatures,
    "Frequent" to frequentSignatures
)

// Wraps the logic for "where can I place a block in a BondGrid such that it does not overlap with
// any existing blocks. Subclasses should each hold logic for a particular kind of block.
interface BlockPlacer {
    // Which cells will contain a bond if the block was placed at (0,0) [meaning the top left of the BondGrid]
    // Used for testing whether a block would overlap with any existing blocks.
    val occupiedCells: List<Pair<Int, Int>>

    // Returns true if a block placed a |row|, |col| would fit in the board without wraparound and
    // without overlapping any existing blocks.
    fun canPlaceSelfWithoutWraparound(bondGrid: BondGrid, row: Int, col: Int): Boolean {
        return occupiedCells.map { (rowOffset, colOffset) ->
            (row + rowOffset < bondGrid.size) && (col + colOffset < bondGrid[0].size) &&
                    bondGrid[row + rowOffset][col + colOffset].isEmpty()
        }.all { it }
    }

    // Returns all positions where a block can fit in the board without wraparound and
    // without overlapping any existing blocks.
    fun validPositionsWithoutWraparound(bondGrid: BondGrid): List<Pair<Int, Int>> {
        return bondGrid.indices.map { row ->
            bondGrid[0].indices.toList().map { col ->
                Pair(row, col)
            }
        }.flatten().filter { canPlaceSelfWithoutWraparound(bondGrid, it.first, it.second) }
    }

    // Returns true if a block can be placed at |row|, |col| without overlapping any existing blocks
    fun canPlaceSelf(bondGrid: BondGrid, row: Int, col: Int): Boolean {
        return occupiedCells.map { (rowOffset, colOffset) ->
            bondGrid[(row + rowOffset) % bondGrid.size][(col + colOffset) % bondGrid[0].size].isEmpty()
        }.all { it }
    }

    // Returns all positions where a block can fit in the board without overlapping existing blocks.
    fun validPositions(bondGrid: BondGrid): List<Pair<Int, Int>> {
        return bondGrid.indices.map { row ->
            bondGrid[0].indices.toList().map { col ->
                Pair(row, col)
            }
        }.flatten().filter { canPlaceSelf(bondGrid, it.first, it.second) }
    }

    // Picks a valid position where a block can be placed. Prefers positions that place the block
    // without it wrapping around any edges.
    // Throws NoSuchElementException if there are no valid positions
    fun pickPosition(bondGrid: BondGrid): Pair<Int, Int>? {
        val noWraparoundPositions = validPositionsWithoutWraparound(bondGrid)
        if (noWraparoundPositions.isNotEmpty()) {
            return noWraparoundPositions.random()
        }

        val positions = validPositions(bondGrid)
        if (positions.isNotEmpty()) {
            return positions.random()
        }

        Log.e("jmerm", "Failed to place Block, ${bondGrid.contentToString()}")
        return null
    }

    // Adds the block to the grid such that it does not overlap any existing blocks.
    fun placeSelf(bondGrid: BondGrid)
}

// Class for adding 1x2 landscape rectangle blocks
class HDominoPlacer : BlockPlacer {
    override val occupiedCells = listOf(Pair(0, 0), Pair(0, 1))
    override fun placeSelf(bondGrid: BondGrid) {
        val (row, col) = pickPosition(bondGrid) ?: return
        val dstCol = (col + 1) % bondGrid[0].size
        bondGrid[row][col] = "R"
        bondGrid[row][dstCol] = "L"
    }
}

// Class for adding 1x2 portrait rectangle blocks
class VDominoPlacer : BlockPlacer {
    override val occupiedCells = listOf(Pair(0, 0), Pair(1, 0))
    override fun placeSelf(bondGrid: BondGrid) {
        val (row, col) = pickPosition(bondGrid) ?: return
        val dstRow = (row + 1) % bondGrid.size
        bondGrid[row][col] = "D"
        bondGrid[dstRow][col] = "U"
    }
}

// Class for adding 2x2 square blocks
class SquarePlacer : BlockPlacer {
    override val occupiedCells = listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(1, 1))
    override fun placeSelf(bondGrid: BondGrid) {
        val (row, col) = pickPosition(bondGrid) ?: return
        val dstRow = (row + 1) % bondGrid.size
        val dstCol = (col + 1) % bondGrid[0].size

        bondGrid[row][col] = "D R"
        bondGrid[dstRow][col] = "U R"
        bondGrid[row][dstCol] = "D L"
        bondGrid[dstRow][dstCol] = "U L"
    }
}

// Class for adding 1x3 landscape rectangle blocks
class HTriplePlacer : BlockPlacer {
    override val occupiedCells = listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2))
    override fun placeSelf(bondGrid: BondGrid) {
        val (row, col) = pickPosition(bondGrid) ?: return
        val dstCol = (col + 1) % bondGrid[0].size
        val dstCol2 = (dstCol + 1) % bondGrid[0].size
        bondGrid[row][col] = "R"
        bondGrid[row][dstCol] = "R L"
        bondGrid[row][dstCol2] = "L"
    }
}

// Class for adding 1x3 portrait rectangle blocks
class VTriplePlacer : BlockPlacer {
    override val occupiedCells = listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0))
    override fun placeSelf(bondGrid: BondGrid) {
        val (row, col) = pickPosition(bondGrid) ?: return
        val dstRow = (row + 1) % bondGrid.size
        val dstRow2 = (dstRow + 1) % bondGrid.size
        bondGrid[row][col] = "D"
        bondGrid[dstRow][col] = "U D"
        bondGrid[dstRow2][col] = "U"
    }
}

// Adds bonds to a board. The set of bonds added depends on the dimensions of the board and the
// |numBlocks| option. The exact position of the bonds is random.
fun addBonds(numRows: Int, numCols: Int, board: List<Int>, numBlocks: String): List<String> {
    val bondGrid = Array(numRows) { Array(numCols) { "" } }
    val signatures = allSignaturesMap.getValue(numBlocks)

    val bondSignature = when {
        signatures.containsKey(BoardSize(numRows, numCols)) -> {
            signatures.getValue(BoardSize(numRows, numCols))
        }
        signatures.containsKey(BoardSize(numCols, numRows)) -> {
            signatures.getValue(BoardSize(numCols, numRows)).transpose()
        }
        else -> {
            Log.e(
                "jmerm",
                "Failed to find BondSignature for params:: $numRows, $numCols, $numBlocks"
            )
            BondSignature(0, 0, 0, 0, 0)
        }
    }

    repeat(bondSignature.numHTriple) {
        HTriplePlacer().placeSelf(bondGrid)
    }
    repeat(bondSignature.numVTriple) {
        VTriplePlacer().placeSelf(bondGrid)
    }
    repeat(bondSignature.numSquare) {
        SquarePlacer().placeSelf(bondGrid)
    }
    repeat(bondSignature.numHDomino) {
        HDominoPlacer().placeSelf(bondGrid)
    }
    repeat(bondSignature.numVDomino) {
        VDominoPlacer().placeSelf(bondGrid)
    }

    return board.zip(bondGrid.flatten()).map {
        if (it.second.isEmpty()) {
            it.first.toString()
        } else {
            "${it.second} ${it.first % 6}"
        }
    }
}
