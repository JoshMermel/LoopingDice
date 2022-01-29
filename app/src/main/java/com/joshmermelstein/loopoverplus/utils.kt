package com.joshmermelstein.loopoverplus

import android.content.Context

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

// Silly wrappers around quantity strings to make usage more concise
fun pluralizedMoves(num: Int, context: Context): String =
    context.resources.getQuantityString(R.plurals.numberOfMoves, num, num)
fun pluralizedRows(num: Int, context: Context): String =
    context.resources.getQuantityString(R.plurals.numberOfRows, num, num)
fun pluralizedCols(num: Int, context: Context): String =
    context.resources.getQuantityString(R.plurals.numberOfColumns, num, num)