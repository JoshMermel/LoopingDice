package com.joshmermelstein.loopoverplus

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import androidx.core.content.ContextCompat

// TODO(jmerm): it would be nice if offset could be allowed to be out of range so we wouldn't have
//  to worry about modulus-ing it back into range when making these in factories.
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
        val shapeDrawable = ShapeDrawable(RectShape())
        shapeDrawable.paint.shader =
            makeShader(
                axis,
                direction,
                ContextCompat.getColor(context, R.color.gameplay_background),
                ContextCompat.getColor(context, R.color.highlight)
            )

        // TODO(jmerm): add getter for bounds width/height and use it here.

        if (axis == Axis.HORIZONTAL) {
            val safeOffset = mod(offset, numRows)

            // determine top and bottom based on numRows and boundsTop/Bottom
            val height = (bounds.bottom - bounds.top) / numRows
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

            val width = (bounds.right - bounds.left) / numCols
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
}