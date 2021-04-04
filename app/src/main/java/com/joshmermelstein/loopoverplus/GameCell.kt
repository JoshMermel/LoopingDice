package com.joshmermelstein.loopoverplus

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color

// A gameCell object represents a single square on the board. GameCells are relatively dumb and
// only know how to draw themselves. Movement is handled by the game manager object.
//
// There are several subclasses implementing GameCell to represent different kinds of cells.

// from https://medium.com/cafe-pixo/inclusive-color-palettes-for-the-web-bbfe8cf2410e
val colors = arrayOf(
    Color.parseColor("#6FDE6E"),
    Color.parseColor("#FF4242"),
    Color.parseColor("#A691AE"),
    Color.parseColor("#235FA4"),
    Color.parseColor("#000000"), // bandaged
    Color.parseColor("#E8F086"), // enabler
)

// A factory method for creating gameCells.
fun makeGameCell(
    x: Int,
    y: Int,
    numRows: Int,
    numCols: Int,
    colorId: String,
    context: Context
): GameCell {
    return when {
        colorId == "E" -> {
            EnablerGameCell(x.toDouble(), y.toDouble(), numRows, numCols, colorId, context)
        }
        colorId.startsWith("F") -> {
            FixedGameCell(x.toDouble(), y.toDouble(), numRows, numCols, colorId, context)
        }
        colorId.startsWith("B") -> {
            BandagedGameCell(x.toDouble(), y.toDouble(), numRows, numCols, colorId, context)
        }
        colorId.startsWith("H") -> {
            HorizontalGameCell(x.toDouble(), y.toDouble(), numRows, numCols, colorId, context)
        }
        colorId.startsWith("V") -> {
            VerticalGameCell(x.toDouble(), y.toDouble(), numRows, numCols, colorId, context)
        }
        else -> NormalGameCell(x.toDouble(), y.toDouble(), numRows, numCols, colorId, context)
    }
}

// Surely there is a better way to do this. This is a hack so a cell can say what kind of cell it is.
enum class CellFamily {
    NORMAL,
    ENABLER,
    FIXED,
    VERTICAL,
    HORIZONTAL
}

// Base class for shared logic among game cell types
abstract class GameCell(
    open var x: Double,
    open var y: Double,
    open val numRows: Int,
    open val numCols: Int,
    val colorId: String
) {
    // While a cell is in motion, it is useful to know where it was when the move started in
    // addition to where it currently is. |x| and |y| track the initial position and |offsetX| and
    // |offsetY| track the delta from there. Offsets get folded into the base position when a move
    // completes.
    var offsetX: Double = 0.0
    var offsetY: Double = 0.0
    abstract val color: Int
    abstract val pips: Int
    abstract val family: CellFamily


    // There's probably a better way to do this but this is an easy way to let the game manager
    // flash icons on cells without having to worry about which type of cell they are.
    var shouldDrawIcon: Boolean = false

    open fun bonds(): List<Bond> {
        return emptyList()
    }

    // The bounds arguments to this function tell the cell how large the board is so it knows how
    // to scale its coordinate. x, y, and offsets are scaled so that 1.0 means the width of one
    // cell.
    fun drawSelf(
        canvas: Canvas,
        bounds: Bounds,
        padding: Int
    ) {
        val width = bounds.width()
        val height = bounds.height()

        val left = width * (x + offsetX) / numCols + padding + bounds.left
        val right = width * (x + offsetX + 1) / numCols - padding + bounds.left
        val top = height * (y + offsetY) / numRows + padding + bounds.top
        val bottom = height * (y + offsetY + 1) / numRows - padding + bounds.top
        drawSelf(canvas, left, top, right, bottom, bounds, padding)
    }

    // Resets the "base" position of the cell once a move has ended.
    fun finalize(numRows: Int, numCols: Int) {
        // The extra adding and modding is to keep cells in a range where they are easy to draw
        // (The draw function get tripped up when both are negative)
        this.x = (this.x + this.offsetX + numCols) % numCols
        this.y = (this.y + this.offsetY + numRows) % numRows

        this.offsetX = 0.0
        this.offsetY = 0.0
    }

    // Draws the shape but clamps boundaries that would have gone outside the game board.
    open fun drawSquareClamped(
        canvas: Canvas, left: Double, top: Double, right: Double, bottom: Double,
        bounds: Bounds, padding: Int
    ) {
        val clampedLeft = left.coerceAtLeast(bounds.left + padding)
        val clampedTop = top.coerceAtLeast(bounds.top + padding)
        val clampedRight = right.coerceAtMost(bounds.right - padding)
        val clampedBottom = bottom.coerceAtMost(bounds.bottom - padding)
        drawSquare(clampedLeft, clampedTop, clampedRight, clampedBottom, canvas)
        drawPips(left, top, right, bottom, this.pips, canvas)
    }

    // Sometimes the boundaries of a cell may go outside the board's boundaries. In that case, we
    // draw the cell twice, once at it original position (clamped into the boards's boundaries) and
    // once on the other side of the board.
    private fun drawSelf(
        canvas: Canvas,
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        bounds: Bounds,
        padding: Int
    ) {
        // Draw at original position
        drawSquareClamped(canvas, left, top, right, bottom, bounds, padding)

        // If the square overlaps any bound, draw a second square
        if ((left < bounds.left) || (right > bounds.right) || (top < bounds.top) || (bottom > bounds.bottom)) {
            var left2 = left
            var right2 = right
            var top2 = top
            var bottom2 = bottom
            when {
                left < bounds.left -> {
                    left2 += bounds.width()
                    right2 += bounds.width()
                }
                right > bounds.right -> {
                    left2 -= bounds.width()
                    right2 -= bounds.width()
                }
                top < bounds.top -> {
                    bottom2 += bounds.height()
                    top2 += bounds.height()
                }
                bottom > bounds.bottom -> {
                    bottom2 -= bounds.height()
                    top2 -= bounds.height()
                }
            }
            drawSquareClamped(canvas, left2, top2, right2, bottom2, bounds, padding)
        }
    }

    // Virtual function for drawing a square. Can be overridden to give subclasses distinctive
    // styles.
    abstract fun drawSquare(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        canvas: Canvas
    )

    // Draws pips in dice style arrangement.
    open fun drawPips(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        numCircles: Int,
        canvas: Canvas
    ) {
        val width = right - left
        val height = bottom - top
        val pipsize = (width / 9)

        when (numCircles) {
            0 -> return
            1 -> drawPip(left + (width / 2), top + (height / 2), pipsize, canvas)
            2 -> {
                drawPip(left + (width / 3), top + (height / 3), pipsize, canvas)
                drawPip(left + (2 * width / 3), top + (2 * height / 3), pipsize, canvas)
            }
            3 -> {
                drawPip(left + (width / 4), top + (3 * height / 4), pipsize, canvas)
                drawPip(left + (width / 2), top + (height / 2), pipsize, canvas)
                drawPip(left + (3 * width / 4), top + (height / 4), pipsize, canvas)
            }
            4 -> {
                drawPip(left + (width / 4), top + (height / 4), pipsize, canvas)
                drawPip(left + (3 * width / 4), top + (height / 4), pipsize, canvas)
                drawPip(left + (width / 4), top + (3 * height / 4), pipsize, canvas)
                drawPip(left + (3 * width / 4), top + (3 * height / 4), pipsize, canvas)
            }
            5 -> {
                drawPip(left + (width / 4), top + (3 * height / 4), pipsize, canvas)
                drawPip(left + (width / 4), top + (height / 4), pipsize, canvas)
                drawPip(left + (width / 2), top + (height / 2), pipsize, canvas)
                drawPip(left + (3 * width / 4), top + (height / 4), pipsize, canvas)
                drawPip(left + (3 * width / 4), top + (3 * height / 4), pipsize, canvas)
            }
            6 -> {
                drawPip(left + (width / 4), top + (height / 4), pipsize, canvas)
                drawPip(left + (width / 4), top + (height / 2), pipsize, canvas)
                drawPip(left + (3 * width / 4), top + (height / 4), pipsize, canvas)
                drawPip(left + (3 * width / 4), top + (height / 2), pipsize, canvas)
                drawPip(left + (width / 4), top + (3 * height / 4), pipsize, canvas)
                drawPip(left + (3 * width / 4), top + (3 * height / 4), pipsize, canvas)
            }
        }
    }

    // virtual method for drawing one pip. Can be overridden by subclasses to give their pips a
    // distinctive style.
    abstract fun drawPip(centerX: Double, centerY: Double, radius: Double, canvas: Canvas)

    // This is used for saving the game.
    override fun toString(): String {
        return this.colorId
    }

    // Used when checking if the player won. Cells with the same colorId are considered identical.
    override operator fun equals(other: Any?): Boolean {
        return (other as GameCell).colorId == this.colorId
    }

    override fun hashCode(): Int {
        return colorId.hashCode()
    }
}