package com.joshmermelstein.loopoverplus

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.floor

// A custom View class for managing interactions between the canvas and gameplay logic.
class GameplayView : View {
    lateinit var gameManager: GameManager

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    // cache of boundaries for drawing the board. Null indicates that they haven't been computed yet.
    private var boundsBoard = Bounds(-1.0, -1.0, -1.0, -1.0)

    private fun placeBoard(canvas: Canvas) {
        val boardHeight = gameManager.board.numRows
        val boardWidth = gameManager.board.numCols
        val highestPossible = canvas.height / 4
        val lowestPossible = canvas.height

        // Assume we'll take the whole horizontal space, ignore vertical spill
        var left = 0
        var top = highestPossible
        var right = canvas.width
        var bottom = top + (boardHeight * canvas.width / boardWidth)

        // If that layout caused overflow, scale down until we fit
        if (bottom > lowestPossible) {
            right = (right.toDouble() * (lowestPossible - top) / (bottom - top)).toInt()
            bottom = lowestPossible
            // also center horizontally
            val margin = canvas.width - right
            left += margin / 2
            right += margin / 2
        } else {
            // also center vertically
            val margin = lowestPossible - bottom
            top += margin / 2
            bottom += margin / 2
        }

        boundsBoard = Bounds(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble())
    }

    // cache of boundaries for drawing the board. Null indicates that they haven't been computed yet.
    private var boundsLegend = Bounds(-1.0, -1.0, -1.0, -1.0)
    private var legendCirclePaint = Paint()

    private fun placeLegend(canvas: Canvas) {
        val boardHeight = gameManager.board.numRows
        val boardWidth = gameManager.board.numCols
        val highestPossible = canvas.height / 6

        var right = highestPossible
        var bottom = highestPossible

        if (boardWidth > boardHeight) {
            bottom *= boardHeight
            bottom /= boardWidth
        } else {
            right *= boardWidth
            right /= boardHeight
        }

        boundsLegend = Bounds(1.0, 1.0, right.toDouble(), bottom.toDouble())

        with(legendCirclePaint) {
            color = ContextCompat.getColor(context, R.color.legend_background)
            style = Paint.Style.FILL
            isAntiAlias = true
            isDither = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        gameManager.update()
        if (boundsBoard.left >= 0.0) {
            // gameManager.drawHighlights(canvas, boundsBoard)
            gameManager.drawBoard(canvas, boundsBoard)
        } else {
            placeBoard(canvas)
        }

        if (boundsLegend.left >= 0.0) {
            canvas.drawCircle(0f, 0f, (height / 4).toFloat(), legendCirclePaint)
            gameManager.drawGoal(canvas, boundsLegend)
        } else {
            placeLegend(canvas)
        }

        invalidate()
    }

    private var eventStartX: Float = 0f
    private var eventStartY: Float = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                eventStartX = event.x
                eventStartY = event.y
                true
            }
            MotionEvent.ACTION_UP -> {
                maybeEnqueueMove(eventStartX, eventStartY, event.x, event.y)
                gameManager.resetPreview()
                true
            }
            MotionEvent.ACTION_MOVE -> {
                gameManager.resetPreview()
                maybeSetPreview(eventStartX, eventStartY, event.x, event.y)
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun closeTo(f: Float, target: Double): Boolean {
        return f > target - allowedAngleError && f < target + allowedAngleError
    }

    private fun isInsideGrid(x: Float, y: Float): Boolean {
        return (x >= boundsBoard.left && x <= boundsBoard.right && y >= boundsBoard.top && y <= boundsBoard.bottom)
    }


    private fun angleToAxis(theta: Float): Axis? {
        return if (closeTo(theta, 0.0)) {
            Axis.HORIZONTAL
        } else if (closeTo(theta, PI / 2) || closeTo(theta, -PI / 2)) {
            Axis.VERTICAL
        } else {
            // some weirdo diagonal, ignore it.
            null
        }
    }

    private fun getDirection(hDist: Float, vDist: Float, axis: Axis): Direction {
        val dist = when (axis) {
            Axis.HORIZONTAL -> hDist
            Axis.VERTICAL -> vDist
        }

        return when {
            dist < 0 -> Direction.FORWARD
            else -> Direction.BACKWARD
        }
    }

    private fun getOffset(startX: Float, startY: Float, axis: Axis): Int {
        return when (axis) {
            Axis.HORIZONTAL -> floor(gameManager.board.numRows * (startY - boundsBoard.top) / (boundsBoard.bottom - boundsBoard.top)).toInt()
            Axis.VERTICAL -> floor(gameManager.board.numCols * (startX - boundsBoard.left) / (boundsBoard.right - boundsBoard.left)).toInt()
        }
    }

    // Glorified tuple for returning the result of a swipe
    data class SwipeResult(
        val axis: Axis,
        val direction: Direction,
        val offset: Int
    )

    // Translates the start/end coordinates of a swipe into a triple of Axis, Direction, Offset,
    // suitable for turning into a Move.
    // Returns Null if the swipe isn't a valid input to make a move.
    private fun interpretSwipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float
    ): SwipeResult? {
        // ignore events that start outside the grid.
        if (!isInsideGrid(startX, startY)) {
            return null
        }

        // Compute swipe angle and distance. Ignore very short swipes.
        val hDist = (startX - endX)
        val vDist = (startY - endY)
        if ((hDist * hDist) + (vDist * vDist) < minSwipeDistance) {
            return null
        }
        val theta = atan(vDist / hDist)

        // Compute Axis and Direction of swipe
        val axis = angleToAxis(theta) ?: return null
        val direction = getDirection(hDist, vDist, axis)
        val offset = getOffset(startX, startY, axis)

        return SwipeResult(axis, direction, offset)
    }

    private fun maybeEnqueueMove(startX: Float, startY: Float, endX: Float, endY: Float) {
        val (axis, direction, offset) = interpretSwipe(startX, startY, endX, endY) ?: return
        gameManager.enqueueMove(axis, direction, offset)

    }

    private fun maybeSetPreview(startX: Float, startY: Float, endX: Float, endY: Float) {
        val (axis, direction, offset) = interpretSwipe(startX, startY, endX, endY) ?: return
        gameManager.setPreview(axis, direction, offset)
    }
}