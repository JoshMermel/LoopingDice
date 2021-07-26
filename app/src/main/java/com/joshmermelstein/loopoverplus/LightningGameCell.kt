package com.joshmermelstein.loopoverplus

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat

class LightningGameCell(
    override var x: Double,
    override var y: Double,
    override val numRows: Int,
    override val numCols: Int,
    colorId: String,
    override val data: GameCellMetadata
) : NormalGameCellBase(x, y, numRows, numCols, colorId) {
    override val color: Int
    override val drawColor: Int
    override val family = CellFamily.LIGHTNING
    override val pips = 1

    init {
        val parts = colorId.split(" ")
        color = parts[1].toInt() % 6
        drawColor = data.colors[color]
    }

    override fun drawPips(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        numCircles: Int,
        canvas: Canvas
    ) {
        drawLightning(left, top, right, bottom, canvas)
    }

    private fun drawLightning(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        canvas: Canvas
    ) {
        drawIcon(data.lightning, left, top, right, bottom, canvas)
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
        DrawableCompat.setTint(icon.mutate(), data.pipColor)
        icon.draw(canvas)
    }
}