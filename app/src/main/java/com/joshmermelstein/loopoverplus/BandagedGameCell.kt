package com.joshmermelstein.loopoverplus

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

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
                    "U" -> Bond.UP
                    "D" -> Bond.DOWN
                    "L" -> Bond.LEFT
                    else -> Bond.RIGHT
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

enum class Bond {
    RIGHT,
    DOWN,
    LEFT,
    UP
}