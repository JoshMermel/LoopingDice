package com.joshmermelstein.loopoverplus

import android.graphics.Canvas
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import androidx.core.graphics.drawable.DrawableCompat

// An enabler cell is like a regular gameCell except it is gold and uses squared off rectangles
// instead of rounded ones.
class EnablerGameCell(
    override var x: Double,
    override var y: Double,
    override val numRows: Int,
    override val numCols: Int,
    colorId: String,
    private val data: GameCellMetadata
) : GameCell(x, y, numRows, numCols, colorId) {
    override val color = 5
    override val pips = 1
    override val drawColor = data.colors[color]
    override val family = CellFamily.ENABLER

    override fun drawSquare(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        canvas: Canvas
    ) {
        ShapeDrawable(RectShape()).apply {
            setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
            paint.color = drawColor
            draw(canvas)
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
        data.key.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        DrawableCompat.setTint(data.key.mutate(), data.pipColor)
        data.key.draw(canvas)
    }
}