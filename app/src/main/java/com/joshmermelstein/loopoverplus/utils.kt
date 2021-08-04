package com.joshmermelstein.loopoverplus

// Misc utils that are handy but don't fit elsewhere.

class Bounds(
    val left: Double,
    val top: Double,
    val right: Double,
    val bottom: Double
) {
    fun width(): Double {
        return right - left
    }

    fun height(): Double {
        return bottom - top
    }
}

// Like % operator but always returns a non-negative number
fun mod(base: Int, modulus: Int): Int {
    return ((base % modulus) + modulus) % modulus
}

fun isNumeric(s: String): Boolean {
    return s.matches("-?\\d+".toRegex())
}

// Returns whether two arrays of strings contain the same elements
fun sameElements(b1: Array<String>, b2: Array<String>): Boolean {
    return b1.groupingBy { it }.eachCount() == b2.groupingBy { it }.eachCount()
}

fun opposite(direction: Direction): Direction {
    return when (direction) {
        Direction.FORWARD -> Direction.BACKWARD
        Direction.BACKWARD -> Direction.FORWARD
    }
}

// TODO(jmerm): replace these function with Quantity Strings:
// https://developer.android.com/guide/topics/resources/string-resource#Plurals
fun pluralizedMoves(num: Int): String {
    return when (num) {
        1 -> "move"
        else -> "moves"
    }
}
fun pluralizedRows(num: Int): String {
    return when (num) {
        1 -> "row"
        else -> "rows"
    }
}
fun pluralizedCols(num: Int): String {
    return when (num) {
        1 -> "column"
        else -> "columns"
    }
}