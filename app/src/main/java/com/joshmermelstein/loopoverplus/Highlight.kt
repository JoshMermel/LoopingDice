package com.joshmermelstein.loopoverplus

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import androidx.core.content.ContextCompat

class Highlight(
    private val axis: Axis,
    private val direction: Direction,
    private val offset: Int
) {
    fun drawSelf(
        canvas: Canvas,
        bounds : Bounds,
        context: Context,
        numRows: Int,
        numCols: Int
    ) {
        val shapeDrawable = ShapeDrawable(RectShape()).apply {
            paint.shader =
                makeShader(
                    axis,
                    direction,
                    ContextCompat.getColor(context, R.color.gameplay_background),
                    ContextCompat.getColor(context, R.color.highlight)
                )
        }

        if (axis == Axis.HORIZONTAL) {
            val safeOffset = mod(offset, numRows)

            // Determine top and bottom based on numRows and boundsTop/Bottom
            val height = bounds.height() / numRows
            val top = (safeOffset * height) + bounds.top
            val bottom = ((safeOffset + 1) * height) + bounds.top
            val left = if (direction == Direction.BACKWARD) {
                bounds.left - 50
            } else {
                bounds.right - 50
            }
            val right = left + 100
            shapeDrawable.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        } else {
            val safeOffset = mod(offset, numCols)

            val width = bounds.width() / numCols
            val left = (safeOffset * width) + bounds.left
            val right = ((safeOffset + 1) * width) + bounds.left
            val top = if (direction == Direction.BACKWARD) {
                bounds.top - 50
            } else {
                bounds.bottom - 50
            }
            val bottom = top + 100
            shapeDrawable.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        }

        shapeDrawable.draw(canvas)
    }

    private fun makeShader(
        axis: Axis,
        direction: Direction,
        background_color: Int,
        highlight_color: Int
    ): Shader {
        var x0 = 0f
        var y0 = 0f
        var x1 = 0f
        var y1 = 0f

        if (axis == Axis.HORIZONTAL && direction == Direction.FORWARD) {
            x0 = 100f
        } else if (axis == Axis.HORIZONTAL && direction == Direction.BACKWARD) {
            x1 = 100f
        } else if (axis == Axis.VERTICAL && direction == Direction.FORWARD) {
            y0 = 100f
        } else {
            y1 = 100f
        }

        return LinearGradient(
            x0, y0, x1, y1,
            intArrayOf(
                background_color,
                highlight_color,
                background_color
            ), null,
            Shader.TileMode.CLAMP
        )
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }
        other as Highlight
        return (axis == other.axis) &&
                (direction == other.direction) &&
                (offset == other.offset)
    }

    override fun hashCode(): Int {
        var result = axis.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + offset
        return result
    }
}