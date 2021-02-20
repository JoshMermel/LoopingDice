package com.joshmermelstein.loopoverplus

// Misc utils that are handy but don't fit elsewhere.
// TODO(jmerm): replace if's with when's in several methods here.

fun opposite(direction: Direction): Direction {
    return if (direction == Direction.FORWARD) {
        Direction.BACKWARD
    } else {
        Direction.FORWARD
    }
}

fun pluralizedMoves(num: Int): String {
    return if (num == 1) {
        "move"
    } else {
        "moves"
    }
}


fun pluralizedRows(num: Int): String {
    return if (num == 1) {
        "row"
    } else {
        "rows"
    }
}

fun pluralizedCols(num: Int): String {
    return if (num == 1) {
        "column"
    } else {
        "columns"
    }
}