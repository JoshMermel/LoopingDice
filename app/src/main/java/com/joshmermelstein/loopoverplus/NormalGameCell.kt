package com.joshmermelstein.loopoverplus

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import androidx.core.content.ContextCompat

// Represents a "normal" gameCell - meaning neither bandaged nor enabler.
open class NormalGameCell(
    override var x: Double,
    override var y: Double,
    override val numRows: Int,
    override val numCols: Int,
    colorId: String,
    override val context: Context
) : NormalGameCellBase(x, y, numRows, numCols, colorId) {
    // TODO(jmerm): this -1 is confusing and silly. What if I update every single level to 0-index instead of 1-indexing.
    override val color: Int = (colorId.toInt() - 1) % 6
    override val pips: Int = ((colorId.toInt() - 1) / 6) + 1
}

// Base class for NormalGameCell that holds all logic but doesn't run initialization code so it's
// fine to subclass it.
abstract class NormalGameCellBase(
    x: Double,
    y: Double,
    numRows: Int,
    numCols: Int,
    colorId: String
) :
    GameCell(x, y, numRows, numCols, colorId) {
    abstract val context: Context
    override val family = CellFamily.NORMAL
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
