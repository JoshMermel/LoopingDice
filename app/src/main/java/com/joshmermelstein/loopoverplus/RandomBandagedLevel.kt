package com.joshmermelstein.loopoverplus

import android.util.Log

/*
 * Generating random banding schemes is such a pain that I'm moving it to its own file to keep
 * everything else organized. The big idea is to map each board size to a set of which bandaged
 * shapes should be added; and also to have a system for randomly placing those blocks.
 */

// TODO(jmerm): something about the fact that all these methods can throw exceptions on failure
// TODO(jmerm): consider having the helpers that add bonds prefer positions without wraparound
// TODO(jmerm): unify all the CanAddFoo functions into something that takes a predicate?

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

// Returns true if a horizontal domino placed at |row|, |col| (and extending right) wouldn't overlap any bonds
fun canAddHDomino(bonds: Array<Array<String>>, row: Int, col: Int): Boolean {
    val dstCol = (col + 1) % bonds[0].size
    return bonds[row][col].isEmpty() && bonds[row][dstCol].isEmpty()
}

// Adds a horizontal domino to a random position in |bonds| such that it doesn't overlap any existing bonds
fun addHDomino(bonds: Array<Array<String>>) {
    val (row, col) = bonds.indices.map { row ->
        bonds[0].indices.toList().map { col ->
            Pair(row, col)
        }
    }.flatten().filter { canAddHDomino(bonds, it.first, it.second) }.random()

    val dstCol = (col + 1) % bonds[0].size
    bonds[row][col] = "R"
    bonds[row][dstCol] = "L"
}

// Returns true if a vertical domino placed at |row|, |col| (and extending down) wouldn't overlap any bonds
fun canAddVDomino(bonds: Array<Array<String>>, row: Int, col: Int): Boolean {
    val dstRow = (row + 1) % bonds.size
    return bonds[row][col].isEmpty() && bonds[dstRow][col].isEmpty()
}

// Adds a horizontal domino to a random position in |bonds| such that it doesn't overlap any existing bonds
fun addVDomino(bonds: Array<Array<String>>) {
    val (row, col) = bonds.indices.map { row ->
        bonds[0].indices.toList().map { col ->
            Pair(row, col)
        }
    }.flatten().filter { canAddVDomino(bonds, it.first, it.second) }.random()

    val dstRow = (row + 1) % bonds.size
    bonds[row][col] = "D"
    bonds[dstRow][col] = "U"
}

// Returns true if a square placed at |row|, |col| (and extending down and right) wouldn't overlap any bonds
fun canAddSquare(bonds: Array<Array<String>>, row: Int, col: Int): Boolean {
    val dstRow = (row + 1) % bonds.size
    val dstCol = (col + 1) % bonds[0].size

    return bonds[row][col].isEmpty() &&
            bonds[dstRow][col].isEmpty() &&
            bonds[row][dstCol].isEmpty() &&
            bonds[dstRow][dstCol].isEmpty()
}

fun addSquare(bonds: Array<Array<String>>) {
    val (row, col) = bonds.indices.map { row ->
        bonds[0].indices.toList().map { col ->
            Pair(row, col)
        }
    }.flatten().filter { canAddSquare(bonds, it.first, it.second) }.random()

    val dstRow = (row + 1) % bonds.size
    val dstCol = (col + 1) % bonds[0].size

    bonds[row][col] = "D R"
    bonds[dstRow][col] = "U R"
    bonds[row][dstCol] = "D L"
    bonds[dstRow][dstCol] = "U L"
}

// Returns true if a horizontal triple placed at |row|, |col| (and extending right) wouldn't overlap any bonds
fun canAddHTriple(bonds: Array<Array<String>>, row: Int, col: Int): Boolean {
    val dstCol = (col + 1) % bonds[0].size
    val dstCol2 = (dstCol + 1) % bonds[0].size
    return bonds[row][col].isEmpty() && bonds[row][dstCol].isEmpty() && bonds[row][dstCol2].isEmpty()
}

fun addHTriple(bonds: Array<Array<String>>) {
    val (row, col) = bonds.indices.map { row ->
        bonds[0].indices.toList().map { col ->
            Pair(row, col)
        }
    }.flatten().filter { canAddHTriple(bonds, it.first, it.second) }.random()

    val dstCol = (col + 1) % bonds[0].size
    val dstCol2 = (dstCol + 1) % bonds[0].size
    bonds[row][col] = "R"
    bonds[row][dstCol] = "R L"
    bonds[row][dstCol2] = "L"
}

// Returns true if a vertical triple placed at |row|, |col| (and extending down) wouldn't overlap any bonds
fun canAddVTriple(bonds: Array<Array<String>>, row: Int, col: Int): Boolean {
    val dstRow = (row + 1) % bonds.size
    val dstRow2 = (dstRow + 1) % bonds.size
    return bonds[row][col].isEmpty() && bonds[dstRow][col].isEmpty() && bonds[dstRow2][col].isEmpty()
}

fun addVTriple(bonds: Array<Array<String>>) {
    val (row, col) = bonds.indices.map { row ->
        bonds[0].indices.toList().map { col ->
            Pair(row, col)
        }
    }.flatten().filter { canAddVTriple(bonds, it.first, it.second) }.random()

    val dstRow = (row + 1) % bonds.size
    val dstRow2 = (dstRow + 1) % bonds.size
    bonds[row][col] = "D"
    bonds[dstRow][col] = "U D"
    bonds[dstRow2][col] = "U"
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
        addHTriple(bonds)
    }
    repeat(bondSignature.numVTriple) {
        addVTriple(bonds)
    }
    repeat(bondSignature.numSquare) {
        addSquare(bonds)
    }
    repeat(bondSignature.numHDomino) {
        addHDomino(bonds)
    }
    repeat(bondSignature.numVDomino) {
        addVDomino(bonds)
    }

    return board.zip(bonds.flatten()).map {
        if (it.second.isEmpty()) {
            it.first.toString()
        } else {
            "B ${mod(it.first - 1, 6) + 1} ${it.second}"
        }
    }
}