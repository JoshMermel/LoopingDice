package com.joshmermelstein.loopoverplus

import android.graphics.Canvas
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import androidx.core.graphics.drawable.DrawableCompat

// A fixed gameCell is like a regular gameCell except it is bandaged and it used squared
// rectangles instead of rounded ones and uses square for pips.
// These are used in Dynamic Bandaging mode and Static Cells mode.
class FixedGameCell(
    override var x: Double,
    override var y: Double,
    override val numRows: Int,
    override val numCols: Int,
    colorId: String,
    private val data : GameCellMetadata
) : GameCell(x, y, numRows, numCols, colorId) {
    override val color = 4
    override val pips: Int
    override val drawColor = data.colors[color]
    override val family = CellFamily.FIXED

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
        shapeDrawable.paint.color = drawColor
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
            shouldDrawIcon -> drawLock(left, top, right, bottom, canvas)
            numCircles == 0 -> drawSmallLock(left, top, right, bottom, canvas)
            else -> super.drawPips(left, top, right, bottom, numCircles, canvas)
        }
    }

    override fun drawPip(centerX: Double, centerY: Double, radius: Double, canvas: Canvas) {
        ShapeDrawable(RectShape()).apply {
            setBounds(
                (centerX - radius).toInt(),
                (centerY - radius).toInt(),
                (centerX + radius).toInt(),
                (centerY + radius).toInt()
            )
            paint.color = data.pipColor
            draw(canvas)
        }
    }

    private fun drawLock(left: Double, top: Double, right: Double, bottom: Double, canvas: Canvas) {
        data.lock.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        DrawableCompat.setTint(data.lock.mutate(), data.pipColor)
        data.lock.draw(canvas)
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
        drawLock((x - radius), (y - radius), (x + radius), (y + radius), canvas)
    }
}
