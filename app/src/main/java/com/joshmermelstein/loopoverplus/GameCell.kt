package com.joshmermelstein.loopoverplus

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import androidx.core.content.ContextCompat

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
    params: GameplayParams,
    colorId: String,
    context: Context
): GameCell {
    return when {
        colorId == "E" -> {
            EnablerGameCell(x.toDouble(), y.toDouble(), params, colorId, context)
        }
        colorId.startsWith("F") -> {
            FixedGameCell(x.toDouble(), y.toDouble(), params, colorId, context)
        }
        colorId.startsWith("B") -> {
            BandagedGameCell(x.toDouble(), y.toDouble(), params, colorId, context)
        }
        else -> NormalGameCell(x.toDouble(), y.toDouble(), params, colorId, context)
    }
}

abstract class NormalGameCellBase(x: Double, y: Double, params: GameplayParams, colorId: String) : GameCell(x, y, params, colorId)
{
    abstract val context : Context
    override fun drawSquare(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        canvas: Canvas
    ) {
        val shapeDrawable = ShapeDrawable(
            RoundRectShape(
                floatArrayOf(
                    40F, 40F, 40F, 40F, 40F, 40F, 40F, 40F
                ), null, null
            )
        )
        shapeDrawable.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        shapeDrawable.paint.color = colors[this.color]
        shapeDrawable.draw(canvas)
    }

    override fun drawPip(centerX: Double, centerY: Double, radius: Double, canvas: Canvas) {
        val shapeDrawable = ShapeDrawable(OvalShape())
        shapeDrawable.setBounds(
            (centerX - radius).toInt(),
            (centerY - radius).toInt(),
            (centerX + radius).toInt(),
            (centerY + radius).toInt()
        )
        shapeDrawable.paint.color = ContextCompat.getColor(context, R.color.gameplay_background)

        shapeDrawable.draw(canvas)
    }
}

// Represents a "normal" gameCell - meaning neither bandaged nor enabler.
open class NormalGameCell(
    override var x: Double, override var y: Double, override val params:
    GameplayParams, colorId: String, override val context: Context
) : NormalGameCellBase(x, y, params, colorId) {
    override val color: Int = colorId.toInt() % 4
    override val pips: Int = ((colorId.toInt() - 1) / 4) + 1
}

enum class Bond {
    RIGHT,
    DOWN,
    LEFT,
    UP
}

// Represents a bandaged gameCell is joined to a neighbor and moves with it.
class BandagedGameCell(
    override var x: Double, override var y: Double, override val params:
    GameplayParams, colorId: String, override val context: Context
) : NormalGameCellBase(x, y, params, colorId) {
    override val color: Int
    override val pips: Int
    private val bonds: MutableList<Bond> = mutableListOf()


    init {
        val parts = colorId.split(" ")
        color = parts[1].toInt() % 4
        pips = ((parts[1].toInt() - 1) / 4) + 1
        for (i in (2 until parts.size)) {
            bonds.add(
                when (parts[i]) {
                    "U" -> {
                        Bond.UP
                    }
                    "D" -> {
                        Bond.DOWN
                    }
                    "L" -> {
                        Bond.LEFT
                    }
                    else -> {
                        Bond.RIGHT
                    }
                }
            )
        }
    }

    override fun bonds(): List<Bond> {
        return bonds
    }

    private fun drawBonds(
        canvas: Canvas,
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        bounds: Bounds,
        padding: Int
    ) {
        val x0 = (right + left) / 2
        val y0 = (bottom + top) / 2

        for (bond in bonds) {
            var x1 = x0
            var y1 = y0
            when (bond) {
                Bond.RIGHT -> {
                    x1 += (right - left + padding)
                }
                Bond.UP -> {
                    y1 -= (bottom - top + padding)
                }
                Bond.LEFT -> {
                    x1 -= (right - left + padding)
                }
                Bond.DOWN -> {
                    y1 += (bottom - top + padding)
                }
            }
            drawLineClamped(
                canvas,
                x0.toFloat(),
                y0.toFloat(),
                x1.toFloat(),
                y1.toFloat(),
                bounds.left.toFloat(),
                bounds.top.toFloat(),
                bounds.right.toFloat(),
                bounds.bottom.toFloat(),
            )
        }
    }

    // Draws the shape but clamps boundaries that would have gone outside the game board.
    override fun drawSquareClamped(
        canvas: Canvas,
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        bounds: Bounds,
        padding: Int
    ) {
        super.drawSquareClamped(canvas, left, top, right, bottom, bounds, padding)
        drawBonds(canvas, left, top, right, bottom, bounds, padding)
    }

    // TODO(jmerm): make last 4 args be a Bounds instead
    private fun drawLineClamped(
        canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float,
        boundsLeft: Float, boundsTop: Float, boundsRight: Float, boundsBottom: Float
    ) {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = (boundsRight - boundsLeft) / 24
        paint.strokeCap = Paint.Cap.ROUND


        val boundedX0 = x0.coerceIn(boundsLeft, boundsRight)
        val boundedX1 = x1.coerceIn(boundsLeft, boundsRight)
        val boundedY0 = y0.coerceIn(boundsTop, boundsBottom)
        val boundedY1 = y1.coerceIn(boundsTop, boundsBottom)

        if (countDifferences(x0, y0, x1, y1, boundedX0, boundedY0, boundedX1, boundedY1) < 2) {
            canvas.drawLine(boundedX0, boundedY0, boundedX1, boundedY1, paint)
        }


        // canvas.drawLine(x0, y0, x1, y1, paint)
    }

    private fun countDifferences(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        boundedX0: Float,
        boundedY0: Float,
        boundedX1: Float,
        boundedY1: Float
    ): Int {
        var ret = 0
        if (x0 != boundedX0) {
            ret += 1
        }
        if (x1 != boundedX1) {
            ret += 1
        }
        if (y0 != boundedY0) {
            ret += 1
        }
        if (y1 != boundedY1) {
            ret += 1
        }
        return ret
    }
}

// Base class for shared logic among game cell types
abstract class GameCell(
    open var x: Double,
    open var y: Double,
    open val params: GameplayParams,
    private val colorId: String
) {
    // While a cell is in motion, it is useful to know where it was when the move started in
    // addition to where it currently is. |x| and |y| track the initial position and |offsetX| and
    // |offsetY| track the delta from there. Offsets get folded into the base position when a move
    // completes.
    var offsetX: Double = 0.0
    var offsetY: Double = 0.0
    abstract val color: Int
    abstract val pips: Int
    open val isBlocking = false
    open val isEnabler = false

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

        val left = width * (x + offsetX) / params.numCols + padding + bounds.left
        val right = width * (x + offsetX + 1) / params.numCols - padding + bounds.left
        val top = height * (y + offsetY) / params.numRows + padding + bounds.top
        val bottom = height * (y + offsetY + 1) / params.numRows - padding + bounds.top
        drawSelf(
            canvas,
            left,
            top,
            right,
            bottom,
            bounds,
            padding
        )
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

        // Possibly draw wraparound
        when {
            left < bounds.left -> {
                drawSquareClamped(
                    canvas,
                    left + bounds.width(),
                    top,
                    right + bounds.width(),
                    bottom,
                    bounds,
                    padding
                )
            }
            right > bounds.right -> {
                drawSquareClamped(
                    canvas,
                    left - bounds.width(),
                    top,
                    right - bounds.width(),
                    bottom,
                    bounds,
                    padding
                )
            }
            top < bounds.top -> {
                drawSquareClamped(
                    canvas,
                    left,
                    top + bounds.height(),
                    right,
                    bottom + bounds.height(),
                    bounds,
                    padding
                )
            }
            bottom > bounds.bottom -> {
                drawSquareClamped(
                    canvas,
                    left,
                    top - bounds.height(),
                    right,
                    bottom - bounds.height(),
                    bounds,
                    padding
                )
            }
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