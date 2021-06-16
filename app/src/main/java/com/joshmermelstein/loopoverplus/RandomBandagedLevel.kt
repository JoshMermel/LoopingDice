package com.joshmermelstein.loopoverplus

/*
 * Generating random banding schemes is such a pain that I'm moving it to its own file to keep
 * everything else organized. The big idea is to map each board size to a set of which bandaged
 * shapes should be added; and also to have a system for randomly placing those blocks.
 */

// Struct for "which block and how many of each".
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
// per-density maps from Pair(numRows, numCols) to a sensible bond signature for a board of those dimensions
val rareSignatures: Map<Pair<Int, Int>, BondSignature> = mapOf(
    Pair(2, 2) to BondSignature(0, 1, 0, 0, 0),
    Pair(3, 2) to BondSignature(0, 1, 0, 0, 0),
    Pair(3, 3) to BondSignature(1, 0, 0, 0, 0),
    Pair(4, 2) to BondSignature(0, 1, 0, 0, 0),
    Pair(4, 3) to BondSignature(2, 0, 0, 0, 0),
    Pair(4, 4) to BondSignature(1, 1, 0, 0, 0),
    Pair(5, 2) to BondSignature(0, 2, 0, 0, 0),
    Pair(5, 3) to BondSignature(1, 1, 0, 0, 1),
    Pair(5, 4) to BondSignature(2, 0, 0, 0, 1),
    Pair(5, 5) to BondSignature(1, 0, 0, 1, 1),
    Pair(6, 2) to BondSignature(0, 1, 0, 0, 1),
    Pair(6, 3) to BondSignature(1, 1, 0, 0, 1),
    Pair(6, 4) to BondSignature(1, 0, 1, 0, 1),
    Pair(6, 5) to BondSignature(2, 2, 0, 0, 1),
)
val commonSignatures: Map<Pair<Int, Int>, BondSignature> = mapOf(
    Pair(2, 2) to BondSignature(1, 0, 0, 0, 0),
    Pair(3, 2) to BondSignature(0, 1, 0, 0, 0),
    Pair(3, 3) to BondSignature(0, 0, 1, 0, 0),
    Pair(4, 2) to BondSignature(0, 1, 0, 0, 0),
    Pair(4, 3) to BondSignature(1, 1, 0, 0, 0),
    Pair(4, 4) to BondSignature(1, 1, 0, 1, 0),
    Pair(5, 2) to BondSignature(0, 1, 0, 0, 1),
    Pair(5, 3) to BondSignature(1, 0, 1, 0, 1),
    Pair(5, 4) to BondSignature(2, 1, 1, 0, 0),
    Pair(5, 5) to BondSignature(2, 1, 0, 1, 1),
    Pair(6, 2) to BondSignature(0, 2, 0, 0, 1),
    Pair(6, 3) to BondSignature(1, 1, 1, 0, 1),
    Pair(6, 4) to BondSignature(1, 0, 1, 1, 1),
    Pair(6, 5) to BondSignature(1, 1, 1, 1, 1),
)
val frequentSignatures: Map<Pair<Int, Int>, BondSignature> = mapOf(
    Pair(2, 2) to BondSignature(2, 0, 0, 0, 0),
    Pair(3, 2) to BondSignature(0, 2, 0, 0, 0),
    Pair(3, 3) to BondSignature(1, 1, 0, 0, 0),
    Pair(4, 2) to BondSignature(0, 2, 0, 0, 0),
    Pair(4, 3) to BondSignature(0, 1, 1, 0, 0),
    Pair(4, 4) to BondSignature(0, 0, 2, 0, 0),
    Pair(5, 2) to BondSignature(0, 2, 0, 0, 1),
    Pair(5, 3) to BondSignature(0, 1, 1, 0, 1),
    Pair(5, 4) to BondSignature(1, 2, 1, 1, 0),
    Pair(5, 5) to BondSignature(1, 2, 1, 1, 0),
    Pair(6, 2) to BondSignature(0, 1, 0, 0, 2),
    Pair(6, 3) to BondSignature(2, 2, 1, 0, 0),
    Pair(6, 4) to BondSignature(1, 1, 1, 1, 1),
    Pair(6, 5) to BondSignature(2, 2, 1, 1, 1),
)

// Ultimate map for figuring out which BondSignature to apply to a board.
val allSignaturesMap = mapOf(
    "Rare" to rareSignatures,
    "Common" to commonSignatures,
    "Frequent" to frequentSignatures
)

// Wraps the logic for "where can I place a block whose bonds include these cells?" and the logic
// for placing those bonds in a 2D array. Subclasses should each hold logic for a particular kind of block.
interface BondPlacer {
    // Which cells will contain a bond, relative to (0,0).
    val occupiedCells: List<Pair<Int, Int>>

    // Returns whether a block placed a |row|, |col| would fit in the board without wraparound and
    // without overlapping any existing blocks.
    fun canPlaceSelfWithoutWraparound(bonds: Array<Array<String>>, row: Int, col: Int): Boolean {
        return occupiedCells.map { (rowOffset, colOffset) ->
            (row + rowOffset < bonds.size) && (col + colOffset < bonds[0].size) &&
                    bonds[row + rowOffset][col + colOffset].isEmpty()
        }.all { it }
    }

    // Returns all positions where a block can fit in the board without wraparound and
    // without overlapping any existing blocks.
    fun validPositionsWithoutWraparound(bonds: Array<Array<String>>): List<Pair<Int, Int>> {
        return bonds.indices.map { row ->
            bonds[0].indices.toList().map { col ->
                Pair(row, col)
            }
        }.flatten().filter { canPlaceSelfWithoutWraparound(bonds, it.first, it.second) }
    }

    // Returns whether a block placed at |row|, |col| will overlap any existing blocks
    fun canPlaceSelf(bonds: Array<Array<String>>, row: Int, col: Int): Boolean {
        return occupiedCells.map { (rowOffset, colOffset) ->
            bonds[(row + rowOffset) % bonds.size][(col + colOffset) % bonds[0].size].isEmpty()
        }.all { it }
    }

    // Returns all positions where a block can fit in the board without overlapping existing blocks.
    fun validPositions(bonds: Array<Array<String>>): List<Pair<Int, Int>> {
        return bonds.indices.map { row ->
            bonds[0].indices.toList().map { col ->
                Pair(row, col)
            }
        }.flatten().filter { canPlaceSelf(bonds, it.first, it.second) }
    }

    // Picks a valid position where a block can be placed. Prefers positions that place the block
    // without it wrapping around any edges.
    // Throws NoSuchElementException if there are no valid positions
    fun pickPosition(bonds: Array<Array<String>>): Pair<Int, Int> {
        val noWraparoundPositions = validPositionsWithoutWraparound(bonds)
        if (noWraparoundPositions.isNotEmpty()) {
            return noWraparoundPositions.random()
        }

        val positions = validPositions(bonds)
        if (positions.isNotEmpty()) {
            return positions.random()
        }

        // TODO(jmerm): think more about this exception
        throw NoSuchElementException()
    }

    fun placeSelf(bonds: Array<Array<String>>)
}

// Class for adding 1x2 landscape rectangle blocks
class HDominoPlacer : BondPlacer {
    override val occupiedCells = listOf(Pair(0, 0), Pair(0, 1))
    override fun placeSelf(bonds: Array<Array<String>>) {
        val (row, col) = pickPosition(bonds)
        val dstCol = (col + 1) % bonds[0].size
        bonds[row][col] = "R"
        bonds[row][dstCol] = "L"
    }
}

// Class for adding 1x2 portrait rectangle blocks
class VDominoPlacer : BondPlacer {
    override val occupiedCells = listOf(Pair(0, 0), Pair(1, 0))
    override fun placeSelf(bonds: Array<Array<String>>) {
        val (row, col) = pickPosition(bonds)
        val dstRow = (row + 1) % bonds.size
        bonds[row][col] = "D"
        bonds[dstRow][col] = "U"
    }
}

// Class for adding 2x2 square blocks
class SquarePlacer : BondPlacer {
    override val occupiedCells = listOf(Pair(0, 0), Pair(1, 0), Pair(0, 1), Pair(1, 1))
    override fun placeSelf(bonds: Array<Array<String>>) {
        val (row, col) = pickPosition(bonds)
        val dstRow = (row + 1) % bonds.size
        val dstCol = (col + 1) % bonds[0].size

        bonds[row][col] = "D R"
        bonds[dstRow][col] = "U R"
        bonds[row][dstCol] = "D L"
        bonds[dstRow][dstCol] = "U L"
    }
}

// Class for adding 1x3 landscape rectangle blocks
class HTriplePlacer : BondPlacer {
    override val occupiedCells = listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2))
    override fun placeSelf(bonds: Array<Array<String>>) {
        val (row, col) = pickPosition(bonds)
        val dstCol = (col + 1) % bonds[0].size
        val dstCol2 = (dstCol + 1) % bonds[0].size
        bonds[row][col] = "R"
        bonds[row][dstCol] = "R L"
        bonds[row][dstCol2] = "L"
    }
}

// Class for adding 1x3 portrait rectangle blocks
class VTriplePlacer : BondPlacer {
    override val occupiedCells = listOf(Pair(0, 0), Pair(1, 0), Pair(2, 0))
    override fun placeSelf(bonds: Array<Array<String>>) {
        val (row, col) = pickPosition(bonds)
        val dstRow = (row + 1) % bonds.size
        val dstRow2 = (dstRow + 1) % bonds.size
        bonds[row][col] = "D"
        bonds[dstRow][col] = "U D"
        bonds[dstRow2][col] = "U"
    }
}

// Adds bonds to a board. The set of bonds added depends on the dimensions of the board and the
// |numBlocks| option. The exact position of the bonds is random.
fun addBonds(numRows: Int, numCols: Int, board: List<Int>, numBlocks: String): List<String> {
    val bonds = Array(numRows) { Array(numCols) { "" } }
    val signatures = allSignaturesMap.getValue(numBlocks)

    val bondSignature = when {
        signatures.containsKey(Pair(numRows, numCols)) -> {
            signatures.getValue(Pair(numRows, numCols))
        }
        signatures.containsKey(Pair(numCols, numRows)) -> {
            signatures.getValue(Pair(numCols, numRows)).transpose()
        }
        else -> {
            // TODO(jmerm): log/toast error here.
            BondSignature(0, 0, 0, 0, 0)
        }
    }

    repeat(bondSignature.numHTriple) {
        HTriplePlacer().placeSelf(bonds)
    }
    repeat(bondSignature.numVTriple) {
        VTriplePlacer().placeSelf(bonds)
    }
    repeat(bondSignature.numSquare) {
        SquarePlacer().placeSelf(bonds)
    }
    repeat(bondSignature.numHDomino) {
        HDominoPlacer().placeSelf(bonds)
    }
    repeat(bondSignature.numVDomino) {
        VDominoPlacer().placeSelf(bonds)
    }

    return board.zip(bonds.flatten()).map {
        if (it.second.isEmpty()) {
            it.first.toString()
        } else {
            "B ${mod(it.first - 1, 6) + 1} ${it.second}"
        }
    }
}