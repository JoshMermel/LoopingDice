package com.joshmermelstein.loopoverplus

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import androidx.core.graphics.drawable.DrawableCompat


// A gameCell object represents a single square on the board. GameCells are relatively dumb and
// only know how to draw themselves. Movement is handled by the game manager object.
class GameCell(
    private var x: Double,
    private var y: Double,
    private val numRows: Int,
    private val numCols: Int,
    private val colorId: String,
    private val data: GameCellMetadata,
) {
    // Parse the colorId to determine what kind of gamecell this is.
    private val config = makeGamecellConfiguration(colorId)

    // Getters for details of what kind of gamecell this is.
    fun isVert(): Boolean = config.isVert
    fun isHoriz(): Boolean = config.isHoriz
    fun hasBondUp(): Boolean = config.hasBondUp
    fun hasBondDown(): Boolean = config.hasBondDown
    fun hasBondLeft(): Boolean = config.hasBondLeft
    fun hasBondRight(): Boolean = config.hasBondRight
    fun isLighting(): Boolean = config.isLighting
    fun isFixed(): Boolean = config.isFixed
    fun isEnabler(): Boolean = config.isEnabler

    // While a cell is in motion, it is useful to know where it was when the move started in
    // addition to where it currently is. |x| and |y| track the initial position and |offsetX| and
    // |offsetY| track the delta from there. Offsets get folded into the base position when a move
    // completes.
    var offsetX: Double = 0.0
    var offsetY: Double = 0.0

    // Some gamecell configurations have an alternate symbol they draw in special circumstances.
    // These booleans control whether an alternate symbol is drawn instead of their usual pips.
    var shouldDrawKey: Boolean = false
    var shouldDrawLock: Boolean = false

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

    fun copy(): GameCell {
        return GameCell(this.x, this.y, numRows, numCols, colorId, data)
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
    private fun drawSquareClamped(
        canvas: Canvas, left: Double, top: Double, right: Double, bottom: Double,
        bounds: Bounds, padding: Int
    ) {
        val clampedLeft = left.coerceAtLeast(bounds.left + padding)
        val clampedTop = top.coerceAtLeast(bounds.top + padding)
        val clampedRight = right.coerceAtMost(bounds.right - padding)
        val clampedBottom = bottom.coerceAtMost(bounds.bottom - padding)

        // Reduces jank from moves bouncing back and flashing colors on the opposite edge
        val eccentricity = (clampedBottom - clampedTop) / (clampedRight - clampedLeft)
        if (eccentricity > (1 / eccentricityThreshold) || eccentricity < eccentricityThreshold) {
            return
        }

        drawCellBackground(clampedLeft, clampedTop, clampedRight, clampedBottom, canvas)
        drawCellSymbols(left, top, right, bottom, canvas)
        drawBonds(canvas, left, top, right, bottom, bounds, padding)
    }

    // Sometimes the boundaries of a cell may go outside the board's boundaries. In that case, we
    // draw the cell twice, once at it original position (clamped into the board's boundaries) and
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

    // Draws the background of the cell. The shape depends on the kind of cell this is.
    private fun drawCellBackground(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        canvas: Canvas
    ) {
        val shapeDrawable = if (config.isEnabler || config.isFixed) {
            ShapeDrawable(RectShape())
        } else {
            val radius = ((bottom - top) / 4).toFloat()
            ShapeDrawable(RoundRectShape(FloatArray(8) { radius }, null, null))
        }

        shapeDrawable.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        shapeDrawable.paint.color = data.colors[config.color]
        shapeDrawable.draw(canvas)
    }

    // Draws symbols on the gamecell. This might be traditional pips but might also be a symbol to
    // indicate special properties of the cell.
    private fun drawCellSymbols(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        canvas: Canvas
    ) {
        when {
            shouldDrawKey -> drawIcon(data.key, left, top, right, bottom, canvas)
            shouldDrawLock -> drawIcon(data.lock, left, top, right, bottom, canvas)
            config.isLighting -> drawIcon(data.lightning, left, top, right, bottom, canvas)
            config.isHoriz -> drawIcon(data.hArrow, left, top, right, bottom, canvas)
            config.isVert -> drawIcon(data.vArrow, left, top, right, bottom, canvas)
            config.isFixed && config.numPips == 0 -> drawSmallLock(left, top, right, bottom, canvas)
            else -> drawPipsTraditional(left, top, right, bottom, canvas)
        }
    }

    // Relative offsets of pips within the gamecell. Not all gamecells configurations need to draw
    // pips so we don't bother creating this unless it's needed. This is a negligible optimization
    // but I wanted to use `by lazy` for fun.
    private val pipOffsets: List<Pair<Double, Double>> by lazy {
        traditionalPipOffsets(config.numPips)
    }

    private fun traditionalPipOffsets(numPips: Int): List<Pair<Double, Double>> {
        return when (numPips) {
            1 -> listOf(Pair(0.5, 0.5))
            2 -> listOf(Pair(1.0 / 3, 1.0 / 3), Pair(2.0 / 3, 2.0 / 3))
            3 -> listOf(Pair(0.25, 0.75), Pair(0.5, 0.5), Pair(0.75, 0.25))
            4 -> listOf(Pair(0.25, 0.25), Pair(0.25, 0.75), Pair(0.75, 0.25), Pair(0.75, 0.75))
            5 -> listOf(
                Pair(0.25, 0.25),
                Pair(0.25, 0.75),
                Pair(0.5, 0.5),
                Pair(0.75, 0.25),
                Pair(0.75, 0.75)
            )
            6 -> listOf(
                Pair(0.25, 0.25),
                Pair(0.25, 0.5),
                Pair(0.25, 0.75),
                Pair(0.75, 0.25),
                Pair(0.75, 0.5),
                Pair(0.75, 0.75)
            )
            else -> emptyList()
        }
    }

    // Draws pips in dice style arrangement.
    private fun drawPipsTraditional(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        canvas: Canvas
    ) {
        val width = right - left
        val height = bottom - top
        for (offset in pipOffsets) {
            drawPip(
                left + (offset.first * width),
                top + (offset.second * height),
                width / 9,
                canvas
            )
        }
    }

    // Draws a single pip. The shape of this pip depends on the config of this gamecell.
    private fun drawPip(centerX: Double, centerY: Double, radius: Double, canvas: Canvas) {
        val shapeDrawable = if (config.isFixed || config.isEnabler) {
            ShapeDrawable(RectShape())
        } else {
            ShapeDrawable(OvalShape())
        }
        shapeDrawable.setBounds(
            (centerX - radius).toInt(),
            (centerY - radius).toInt(),
            (centerX + radius).toInt(),
            (centerY + radius).toInt()
        )
        shapeDrawable.paint.color = data.pipColor

        shapeDrawable.draw(canvas)
    }

    // Draws bonds but clamps lines that would have gone outside the game board.
    private fun drawBonds(
        canvas: Canvas,
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        bounds: Bounds,
        padding: Int
    ) {
        val pipX = (right + left) / 2
        val pipY = (bottom + top) / 2
        val bondLen = bottom - top + padding
        val stroke = (right - left).toFloat() / 6

        if (config.hasBondUp) {
            drawLineClamped(canvas, pipX, pipY, pipX, pipY - bondLen, stroke, bounds)
        }
        if (config.hasBondDown) {
            drawLineClamped(canvas, pipX, pipY, pipX, pipY + bondLen, stroke, bounds)
        }
        if (config.hasBondLeft) {
            drawLineClamped(canvas, pipX, pipY, pipX - bondLen, pipY, stroke, bounds)
        }
        if (config.hasBondRight) {
            drawLineClamped(canvas, pipX, pipY, pipX + bondLen, pipY, stroke, bounds)
        }
    }

    // Draws a line from (x0,y0) to (x1,y1) but clamped to fit inside of |bounds|
    private fun drawLineClamped(
        canvas: Canvas,
        x0: Double,
        y0: Double,
        x1: Double,
        y1: Double,
        strokeWidth: Float,
        bounds: Bounds
    ) {
        val paint = Paint()
        paint.color = data.bondColor
        paint.strokeWidth = strokeWidth
        paint.strokeCap = Paint.Cap.ROUND

        // Coerce the endpoints of the line so they fit in the bounding box where the game board is
        // drawn. This approach only works because lines are guaranteed to be horizontal/vertical;
        // otherwise this would mess with slope.
        val boundedX0 = x0.coerceIn(bounds.left, bounds.right)
        val boundedX1 = x1.coerceIn(bounds.left, bounds.right)
        val boundedY0 = y0.coerceIn(bounds.top, bounds.bottom)
        val boundedY1 = y1.coerceIn(bounds.top, bounds.bottom)

        // It's possible that the coercion above left us with a nub of a line that sits on the edge
        // of the board's bounding box and which shouldn't be drawn. One cheap way to check for this
        // is to count how much was changed by the coercion.
        if (countDifferences(x0, y0, x1, y1, boundedX0, boundedY0, boundedX1, boundedY1) < 2) {
            drawLine(canvas, boundedX0, boundedY0, boundedX1, boundedY1, paint)
        }
    }

    private fun countDifferences(
        x0: Double,
        y0: Double,
        x1: Double,
        y1: Double,
        boundedX0: Double,
        boundedY0: Double,
        boundedX1: Double,
        boundedY1: Double
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

    // Canvas APIs are picky about types which force me to use a lot of toType(). This method hides
    // that ugliness for drawing lines using Double coordinates.
    private fun drawLine(
        canvas: Canvas,
        x0: Double,
        y0: Double,
        x1: Double,
        y1: Double,
        paint: Paint
    ) {
        canvas.drawLine(x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat(), paint)
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
        drawIcon(data.lock, (x - radius), (y - radius), (x + radius), (y + radius), canvas)
    }


    private fun drawIcon(
        icon: Drawable,
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        canvas: Canvas
    ) {
        icon.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        DrawableCompat.setTint(icon.mutate(), data.pipColor)

        icon.draw(canvas)
    }

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
