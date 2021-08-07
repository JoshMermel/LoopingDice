package com.joshmermelstein.loopoverplus

/*
// An Arrows gamecell is limited to either its row or its column. They are used in the Arrows
// levels. It indicates which one by drawing an arrow icon instead of pips. When it is swiped
// perpendicular to its axis, it flashes a lock icon instead of of the arrows.
abstract class ArrowsGameCell(
    override var x: Double,
    override var y: Double,
    override val numRows: Int,
    override val numCols: Int,
    colorId: String,
    final override val data: GameCellMetadata
) : NormalGameCellBase(x, y, numRows, numCols, colorId) {
    final override val color: Int
    final override val drawColor: Int

    override val pips = 1
    abstract val arrows: Drawable

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
        when {
            shouldDrawIcon -> drawLock(left, top, right, bottom, canvas)
            else -> drawArrows(left, top, right, bottom, canvas)
        }
    }

    private fun drawLock(left: Double, top: Double, right: Double, bottom: Double, canvas: Canvas) {
        drawIcon(data.lock, left, top, right, bottom, canvas)
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
        DrawableCompat.setTint(icon.mutate(), data.pipColor)
        icon.draw(canvas)
    }
}

// A horizontal gamecell is limited to horizontal moves. Otherwise, it behaves like a normal game cell.
class HorizontalGameCell(
    override var x: Double,
    override var y: Double,
    override val numRows: Int,
    override val numCols: Int,
    colorId: String,
    data: GameCellMetadata
) : ArrowsGameCell(x, y, numRows, numCols, colorId, data) {
    override val family = CellFamily.HORIZONTAL
    override val arrows = super.data.hArrow
}

// A horizontal gamecell is limited to vertical moves. Otherwise, it behaves like a normal game cell.
class VerticalGameCell(
    override var x: Double,
    override var y: Double,
    override val numRows: Int,
    override val numCols: Int,
    colorId: String,
    data: GameCellMetadata
) : ArrowsGameCell(x, y, numRows, numCols, colorId, data) {
    override val family = CellFamily.VERTICAL
    override val arrows = super.data.vArrow
}

 */