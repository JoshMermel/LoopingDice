package com.joshmermelstein.loopoverplus

// Misc utils that are handy but don't fit elsewhere.

fun opposite(direction: Direction): Direction {
    return if (direction == Direction.FORWARD) {
        Direction.BACKWARD
    } else {
        Direction.FORWARD
    }
}

fun axisToString(axis: Axis): String {
    return if (axis == Axis.HORIZONTAL) {
        "H"
    } else {
        "V"
    }
}

fun directionToString(direction: Direction): String {
    return if (direction == Direction.FORWARD) {
        "F"
    } else {
        "B"
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