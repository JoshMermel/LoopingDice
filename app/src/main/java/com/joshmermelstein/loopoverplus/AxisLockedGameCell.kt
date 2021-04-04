package com.joshmermelstein.loopoverplus

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat

abstract class AxisLockedGameCell(
    override var x: Double,
    override var y: Double,
    override val numRows: Int,
    override val numCols: Int,
    colorId: String,
    override val context: Context
) : NormalGameCellBase(x, y, numRows, numCols, colorId) {
    override val color: Int
    override val pips = 1
    abstract val arrows: Drawable
    abstract  val lock: Drawable

    init {
        Log.d("jmerm", "made an axis locked game cell")
        val parts = colorId.split(" ")
        color = parts[1].toInt() % 4
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
            else -> drawArrows(left, top, right, bottom, canvas)
        }
    }

    private fun drawLock(left: Double, top: Double, right: Double, bottom: Double, canvas: Canvas) {
        drawIcon(lock, left, top, right, bottom, canvas)
    }

    private fun drawArrows(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        canvas: Canvas
    ) {
        drawIcon(arrows, left, top, right, bottom, canvas)
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
        DrawableCompat.setTint(
            icon.mutate(),
            ContextCompat.getColor(context, R.color.gameplay_background)
        )
        icon.draw(canvas)
    }
}

class HorizontalGameCell(
    override var x: Double,
    override var y: Double,
    override val numRows: Int,
    override val numCols: Int,
    colorId: String,
    override val context: Context
) : AxisLockedGameCell(x, y, numRows, numCols, colorId, context) {
    override val arrows = ResourcesCompat.getDrawable(
        context.resources,
        R.drawable.ic_baseline_swap_horiz_24,
        null
    )!!
    override val lock = ResourcesCompat.getDrawable(
    context.resources,
    R.drawable.ic_baseline_lock_24,
    null
    )!!
    override val family = CellFamily.HORIZONTAL
}

class VerticalGameCell(
    override var x: Double,
    override var y: Double,
    override val numRows: Int,
    override val numCols: Int,
    colorId: String,
    override val context: Context
) : AxisLockedGameCell(x, y, numRows, numCols, colorId, context) {
    override val arrows = ResourcesCompat.getDrawable(
        context.resources,
        R.drawable.ic_baseline_swap_vert_24,
        null
    )!!
    override val lock = ResourcesCompat.getDrawable(
        context.resources,
        R.drawable.ic_baseline_lock_24,
        null
    )!!
    override val family = CellFamily.VERTICAL
}