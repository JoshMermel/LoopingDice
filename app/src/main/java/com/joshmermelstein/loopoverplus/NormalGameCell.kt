package com.joshmermelstein.loopoverplus

import android.graphics.Canvas
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape

// Represents a "normal" gameCell - meaning neither bandaged nor enabler.
open class NormalGameCell(
    override var x: Double,
    override var y: Double,
    override val numRows: Int,
    override val numCols: Int,
    colorId: String,
    colors : Array<Int>,
    override val pipColor: Int
) : NormalGameCellBase(x, y, numRows, numCols, colorId) {
    final override val color: Int = colorId.toInt() % 6
    override val pips: Int = (colorId.toInt() / 6) + 1
    override val drawColor : Int = colors[color]
}

// Base class for NormalGameCell that holds all logic but doesn't run initialization code so it's
// fine to subclass it.
abstract class NormalGameCellBase(
    x: Double,
    y: Double,
    numRows: Int,
    numCols: Int,
    colorId: String
) : GameCell(x, y, numRows, numCols, colorId) {

    abstract val pipColor: Int
    override val family = CellFamily.NORMAL
    override fun drawSquare(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        canvas: Canvas
    ) {
        val radius = ((bottom - top) / 4).toFloat()
        val shapeDrawable = ShapeDrawable(
            RoundRectShape(
                FloatArray(8) { radius }, null, null
            )
        )
        shapeDrawable.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        // TODO(jmerm): think about black cells in infinity in night mode. Is there a natural way
        //  to invert those?
        shapeDrawable.paint.color = drawColor
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
        shapeDrawable.paint.color = pipColor

        shapeDrawable.draw(canvas)
    }
}
