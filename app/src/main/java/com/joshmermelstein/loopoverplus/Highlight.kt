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
        boundsLeft: Int,
        boundsTop: Int,
        boundsRight: Int,
        boundsBottom: Int,
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

        if (axis == Axis.HORIZONTAL) {
            val safeOffset = mod(offset, numRows)

            // determine top and bottom based on numRows and boundsTop/Bottom
            val height = (boundsBottom - boundsTop) / numRows
            val top = (safeOffset * height) + boundsTop
            val bottom = ((safeOffset + 1) * height) + boundsTop
            val left = if (direction == Direction.BACKWARD) {
                boundsLeft - 50
            } else {
                boundsRight - 50
            }
            val right = left + 100
            shapeDrawable.setBounds(left, top, right, bottom)
        } else {
            val safeOffset = mod(offset, numCols)

            val width = (boundsRight - boundsLeft) / numCols
            val left = (safeOffset * width) + boundsLeft
            val right = ((safeOffset + 1) * width) + boundsLeft
            val top = if (direction == Direction.BACKWARD) {
                boundsTop - 50
            } else {
                boundsBottom - 50
            }
            val bottom = top + 100
            shapeDrawable.setBounds(left, top, right, bottom)
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