package com.joshmermelstein.loopoverplus

/*
 * Generating random banding schemes is such a pain that I'm moving it to its own file to keep
 * everything else organized. The big idea is to map each board size to a set of which bandaged
 * shapes should be added; and also to have a system for randomly placing those blocks.
 */

// TODO(jmerm): something about the fact that all these methods can throw exceptions on failure

class BondSignature(
    val numHDomino: Int,
    val numVDomino: Int,
    val numSquare: Int,
    val numHTriple: Int,
    val numVTriple: Int
) {
    fun transpose(): BondSignature {
        return BondSignature(numVDomino, numHDomino, numSquare, numVTriple, numHTriple)
    }
}


// maps from Pair(numRows, numCols) to a sensible bond signature for a board of those dimensions
// TODO(jmerm): map to a list that we choose randomly from for variety?
// TODO(jmerm): variants for density :(
val signatures: Map<Pair<Int, Int>, BondSignature> = mapOf(
    Pair(2, 2) to BondSignature(1, 0, 0, 0, 0),
    Pair(3, 2) to BondSignature(0, 1, 0, 0, 0),
    Pair(3, 3) to BondSignature(1, 1, 0, 0, 0),
    Pair(4, 2) to BondSignature(0, 2, 0, 0, 0),
    Pair(4, 3) to BondSignature(0, 1, 1, 0, 0),
    Pair(4, 4) to BondSignature(0, 2, 0, 1, 0),
    Pair(5, 2) to BondSignature(0, 3, 0, 0, 0),
    Pair(5, 3) to BondSignature(0, 0, 1, 0, 1),
    Pair(5, 4) to BondSignature(1, 1, 1, 1, 0),
    Pair(5, 5) to BondSignature(1, 2, 0, 1, 1),
    Pair(6, 2) to BondSignature(0, 2, 0, 0, 1),
    Pair(6, 3) to BondSignature(1, 1, 1, 0, 1),
    Pair(6, 4) to BondSignature(1, 1, 1, 1, 1),
    Pair(6, 5) to BondSignature(2, 2, 1, 1, 1),
)

interface BondPlacer {
    val occupiedCells: List<Pair<Int, Int>>

    // TODO(jmerm): this method needs to check for out of bounds!
    fun canPlaceSelfWithoutWraparound(bonds: Array<Array<String>>, row: Int, col: Int): Boolean {
        return occupiedCells.map { (rowOffset, colOffset) ->
            (row + rowOffset < bonds.size) && (col + colOffset < bonds[0].size) &&
                    bonds[row + rowOffset][col + colOffset].isEmpty()
        }.all { it }
    }

    fun validPositionsWithoutWraparound(bonds: Array<Array<String>>): List<Pair<Int, Int>> {
        return bonds.indices.map { row ->
            bonds[0].indices.toList().map { col ->
                Pair(row, col)
            }
        }.flatten().filter { canPlaceSelfWithoutWraparound(bonds, it.first, it.second) }
    }

    fun canPlaceSelf(bonds: Array<Array<String>>, row: Int, col: Int): Boolean {
        return occupiedCells.map { (rowOffset, colOffset) ->
            bonds[(row + rowOffset) % bonds.size][(col + colOffset) % bonds[0].size].isEmpty()
        }.all { it }
    }

    fun validPositions(bonds: Array<Array<String>>): List<Pair<Int, Int>> {
        return bonds.indices.map { row ->
            bonds[0].indices.toList().map { col ->
                Pair(row, col)
            }
        }.flatten().filter { canPlaceSelf(bonds, it.first, it.second) }
    }

    fun pickPosition(bonds: Array<Array<String>>): Pair<Int, Int> {
        val noWraparoundPositions = validPositionsWithoutWraparound(bonds)
        if (noWraparoundPositions.isNotEmpty()) {
            return noWraparoundPositions.random()
        }

        val positions = validPositions(bonds)
        if (positions.isNotEmpty()) {
            return positions.random()
        }

        throw NoSuchElementException()
    }

    fun placeSelf(bonds: Array<Array<String>>)
}

class HDominoPlacer : BondPlacer {
    override val occupiedCells = listOf(Pair(0, 0), Pair(0,1))
    override fun placeSelf(bonds: Array<Array<String>>) {
        val (row, col) = pickPosition(bonds)
        val dstCol = (col + 1) % bonds[0].size
        bonds[row][col] = "R"
        bonds[row][dstCol] = "L"
    }
}

class VDominoPlacer : BondPlacer {
    override val occupiedCells = listOf(Pair(0, 0), Pair(1,0))
    override fun placeSelf(bonds: Array<Array<String>>) {
        val (row, col) = pickPosition(bonds)
        val dstRow = (row + 1) % bonds.size
        bonds[row][col] = "D"
        bonds[dstRow][col] = "U"
    }
}

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

class VTriplePlacer : BondPlacer {
    override val occupiedCells = listOf(Pair(0, 0), Pair(1,0), Pair(2,0))
    override fun placeSelf(bonds: Array<Array<String>>) {
        val (row, col) = pickPosition(bonds)
        val dstRow = (row + 1) % bonds.size
        val dstRow2 = (dstRow + 1) % bonds.size
        bonds[row][col] = "D"
        bonds[dstRow][col] = "U D"
        bonds[dstRow2][col] = "U"
    }
}


fun addBonds(numRows: Int, numCols: Int, board: List<Int>): List<String> {
    val bonds = Array(numRows) { Array(numCols) { "" } }

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