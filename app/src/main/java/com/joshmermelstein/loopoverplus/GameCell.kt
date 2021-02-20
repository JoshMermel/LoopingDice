package com.joshmermelstein.loopoverplus

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat

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

// Represents a "normal" gameCell - meaning neither bandaged nor enabler.
open class NormalGameCell(
    override var x: Double, override var y: Double, override val params:
    GameplayParams, colorId: String, private val context: Context
) : GameCell(x, y, params, colorId) {
    override val color: Int = colorId.toInt() % 4
    override val pips: Int = ((colorId.toInt() - 1) / 4) + 1

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

// A fixed gameCell is like a regular gameCell except it is bandaged and it used squared
// rectangles instead of rounded ones and uses square for pips.
class FixedGameCell(
    override var x: Double, override var y: Double, override val params:
    GameplayParams, colorId: String, private val context: Context
) : GameCell(x, y, params, colorId) {
    override val color = 4
    override val pips: Int
    override val isBlocking = true
    override val isEnabler = false
    private val lock: Drawable = ResourcesCompat.getDrawable(
        context.resources,
        R.drawable.ic_baseline_lock_24,
        null
    )!!

    init {
        val parts = colorId.split(" ")
        pips = parts[1].toInt()
    }

    override fun drawSquare(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        canvas: Canvas
    ) {
        val shapeDrawable = ShapeDrawable(RectShape())
        shapeDrawable.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        shapeDrawable.paint.color = ContextCompat.getColor(context, R.color.bandaged_cell)
        shapeDrawable.draw(canvas)
    }

    override fun drawPips(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        numCircles: Int,
        canvas: Canvas
    ) {
        when {
            shouldDrawIcon -> {
                // Draws a lock instead of pips.
                drawLock(left, top, right, bottom, canvas)
            }
            numCircles == 0 -> {
                drawSmallLock(left, top, right, bottom, canvas)
            }
            else -> {
                super.drawPips(left, top, right, bottom, numCircles, canvas)
            }
        }
    }

    override fun drawPip(centerX: Double, centerY: Double, radius: Double, canvas: Canvas) {
        val shapeDrawable = ShapeDrawable(RectShape())

        shapeDrawable.setBounds(
            (centerX - radius).toInt(),
            (centerY - radius).toInt(),
            (centerX + radius).toInt(),
            (centerY + radius).toInt()
        )
        shapeDrawable.paint.color = ContextCompat.getColor(context, R.color.gameplay_background)

        shapeDrawable.draw(canvas)
    }

    private fun drawLock(left: Double, top: Double, right: Double, bottom: Double, canvas: Canvas) {
        lock.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        DrawableCompat.setTint(
            lock.mutate(),
            ContextCompat.getColor(context, R.color.gameplay_background)
        )
        lock.draw(canvas)
    }

    private fun drawSmallLock(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        canvas: Canvas
    ) {
        val x = (left + right) / 2
        val y = (top + bottom) / 2
        val radius = (right - left) / 4
        lock.setBounds(
            (x - radius).toInt(),
            (y - radius).toInt(),
            (x + radius).toInt(),
            (y + radius).toInt()
        )
        DrawableCompat.setTint(
            lock.mutate(),
            ContextCompat.getColor(context, R.color.gameplay_background)
        )
        lock.draw(canvas)
    }
}

// An enabler cell is like a regular gameCell except it is gold and uses squared off rectangles
// instead of rounded ones.
class EnablerGameCell(
    override var x: Double, override var y: Double, override val params:
    GameplayParams, colorId: String, private val context: Context
) : GameCell(x, y, params, colorId) {
    override val color = 5
    override val pips = 1
    override val isBlocking = false
    override val isEnabler = true
    private val key: Drawable = ResourcesCompat.getDrawable(
        context.resources,
        R.drawable.ic_baseline_vpn_key_24,
        null
    )!!

    override fun drawSquare(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        canvas: Canvas
    ) {
        val shapeDrawable = ShapeDrawable(RectShape())
        shapeDrawable.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        shapeDrawable.paint.color = colors[this.color]
        shapeDrawable.draw(canvas)
    }

    override fun drawPip(centerX: Double, centerY: Double, radius: Double, canvas: Canvas) {
        val shapeDrawable = ShapeDrawable(RectShape())
        shapeDrawable.setBounds(
            (centerX - radius).toInt(),
            (centerY - radius).toInt(),
            (centerX + radius).toInt(),
            (centerY + radius).toInt()
        )
        shapeDrawable.paint.color = ContextCompat.getColor(context, R.color.gameplay_background)

        shapeDrawable.draw(canvas)
    }

    override fun drawPips(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        numCircles: Int,
        canvas: Canvas
    ) {
        if (shouldDrawIcon) {
            // Draws a key icon instead of pips
            drawKey(left, top, right, bottom, canvas)
        } else {
            super.drawPips(left, top, right, bottom, numCircles, canvas)
        }
    }

    private fun drawKey(left: Double, top: Double, right: Double, bottom: Double, canvas: Canvas) {
        key.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        DrawableCompat.setTint(
            key.mutate(),
            ContextCompat.getColor(context, R.color.gameplay_background)
        )
        key.draw(canvas)
    }
}

enum class Bond {
    RIGHT,
    DOWN,
    LEFT,
    UP
}

// Represents a bandaged gameCell is joined to a neighbor and moves with it.
// TODO(jmerm): shared base class with normal game cell for drawing stuff?
class BandagedGameCell(
    override var x: Double, override var y: Double, override val params:
    GameplayParams, colorId: String, private val context: Context
) : GameCell(x, y, params, colorId) {
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

    override fun drawBonds(
        canvas: Canvas, left: Double, top: Double, right: Double, bottom: Double,
        boundsLeft: Int, boundsTop: Int, boundsRight: Int, boundsBottom: Int, padding: Int
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
                boundsLeft.toFloat(),
                boundsTop.toFloat(),
                boundsRight.toFloat(),
                boundsBottom.toFloat(),
            )
        }
    }

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
        boundsLeft: Int,
        boundsTop: Int,
        boundsRight: Int,
        boundsBottom: Int,
        padding: Int
    ) {
        val width = boundsRight - boundsLeft
        val height = boundsBottom - boundsTop

        val left = width * (x + offsetX) / params.numCols + padding + boundsLeft
        val right = width * (x + offsetX + 1) / params.numCols - padding + boundsLeft
        val top = height * (y + offsetY) / params.numRows + padding + boundsTop
        val bottom = height * (y + offsetY + 1) / params.numRows - padding + boundsTop
        drawSelf(
            canvas,
            left,
            top,
            right,
            bottom,
            boundsLeft,
            boundsTop,
            boundsRight,
            boundsBottom,
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
        boundsLeft: Int, boundsTop: Int, boundsRight: Int, boundsBottom: Int, padding: Int
    ) {
        val clampedLeft = left.coerceAtLeast(boundsLeft.toDouble() + padding)
        val clampedTop = top.coerceAtLeast(boundsTop.toDouble() + padding)
        val clampedRight = right.coerceAtMost(boundsRight.toDouble() - padding)
        val clampedBottom = bottom.coerceAtMost(boundsBottom.toDouble() - padding)
        drawSquare(clampedLeft, clampedTop, clampedRight, clampedBottom, canvas)
        drawPips(left, top, right, bottom, this.pips, canvas)
        drawBonds(
            canvas,
            left,
            top,
            right,
            bottom,
            boundsLeft,
            boundsTop,
            boundsRight,
            boundsBottom,
            padding
        )
    }

    open fun drawBonds(
        canvas: Canvas, left: Double, top: Double, right: Double, bottom: Double,
        boundsLeft: Int, boundsTop: Int, boundsRight: Int, boundsBottom: Int, padding: Int
    ) {
    }

    // Sometimes the boundaries of a cell may go outside the board's boundaries. In that case, we
    // draw the cell twice, once at it original position (clamped into the boards's boundaries) and
    // once on the other side of the board.
    private fun drawSelf(
        canvas: Canvas, left: Double, top: Double, right: Double, bottom: Double,
        boundsLeft: Int, boundsTop: Int, boundsRight: Int, boundsBottom: Int, padding: Int
    ) {
        val width = boundsRight - boundsLeft
        val height = boundsBottom - boundsTop
        when {
            left < boundsLeft -> {
                drawSquareClamped(
                    canvas,
                    left,
                    top,
                    right,
                    bottom,
                    boundsLeft,
                    boundsTop,
                    boundsRight,
                    boundsBottom,
                    padding
                )
                drawSquareClamped(
                    canvas,
                    left + width,
                    top,
                    right + width,
                    bottom,
                    boundsLeft,
                    boundsTop,
                    boundsRight,
                    boundsBottom,
                    padding
                )
            }
            right > boundsRight -> {
                drawSquareClamped(
                    canvas,
                    left,
                    top,
                    right,
                    bottom,
                    boundsLeft,
                    boundsTop,
                    boundsRight,
                    boundsBottom,
                    padding
                )
                drawSquareClamped(
                    canvas,
                    left - width,
                    top,
                    right - width,
                    bottom,
                    boundsLeft,
                    boundsTop,
                    boundsRight,
                    boundsBottom,
                    padding
                )
            }
            top < boundsTop -> {
                drawSquareClamped(
                    canvas,
                    left,
                    top,
                    right,
                    bottom,
                    boundsLeft,
                    boundsTop,
                    boundsRight,
                    boundsBottom,
                    padding
                )
                drawSquareClamped(
                    canvas,
                    left,
                    top + height,
                    right,
                    bottom + height,
                    boundsLeft,
                    boundsTop,
                    boundsRight,
                    boundsBottom,
                    padding
                )
            }
            bottom > boundsBottom -> {
                drawSquareClamped(
                    canvas,
                    left,
                    top,
                    right,
                    bottom,
                    boundsLeft,
                    boundsTop,
                    boundsRight,
                    boundsBottom,
                    padding
                )
                drawSquareClamped(
                    canvas,
                    left,
                    top - height,
                    right,
                    bottom - height,
                    boundsLeft,
                    boundsTop,
                    boundsRight,
                    boundsBottom,
                    padding
                )
            }
            else -> {
                drawSquareClamped(
                    canvas,
                    left,
                    top,
                    right,
                    bottom,
                    boundsLeft,
                    boundsTop,
                    boundsRight,
                    boundsBottom,
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