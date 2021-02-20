package com.joshmermelstein.loopoverplus

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat

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
        drawLock((x - radius), (y - radius), (x + radius), (y + radius), canvas)
    }
}