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
            drawLineClamped(canvas, x0, y0, x1, y1, bounds)
        }
    }

    private fun drawLineClamped(canvas: Canvas, x0: Double, y0: Double, x1: Double, y1: Double, bounds: Bounds) {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.strokeWidth = bounds.width().toFloat() / 24
        paint.strokeCap = Paint.Cap.ROUND

        val boundedX0 = x0.coerceIn(bounds.left, bounds.right)
        val boundedX1 = x1.coerceIn(bounds.left, bounds.right)
        val boundedY0 = y0.coerceIn(bounds.top, bounds.bottom)
        val boundedY1 = y1.coerceIn(bounds.top, bounds.bottom)

        if (countDifferences(x0, y0, x1, y1, boundedX0, boundedY0, boundedX1, boundedY1) < 2) {
            drawLine(canvas, boundedX0, boundedY0, boundedX1, boundedY1, paint)
        }
    }

    // Canvas APIs are picky about types which force me to use a lot of toType(). This method hides
    // that ugliness for drawing lines using Double coordinates.
    private fun drawLine(canvas: Canvas, x0: Double, y0: Double, x1: Double, y1: Double, paint: Paint) {
        canvas.drawLine(x0.toFloat(), y0.toFloat(), x1.toFloat(), y1.toFloat(), paint)
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
}

enum class Bond {
    RIGHT,
    DOWN,
    LEFT,
    UP
}