package com.joshmermelstein.loopoverplus

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
    lateinit var gameManager: GameManager // = (context as GameplayActivity).gameManager

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    // cache of boundaries for drawing the board. Null indicates that they haven't been computed yet.
    private var boardLeft: Int = -1
    private var boardTop: Int = -1
    private var boardRight: Int = -1
    private var boardBottom: Int = -1

    private fun placeBoard(canvas: Canvas) {
        val boardWidth = gameManager.board[0].size
        val boardHeight = gameManager.board.size
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

        boardLeft = left
        boardTop = top
        boardRight = right
        boardBottom = bottom
    }

    // cache of boundaries for drawing the board. Null indicates that they haven't been computed yet.
    private var legendRight: Int = -1
    private var legendBottom: Int = -1
    private var legendCirclePaint = Paint()

    private fun placeLegend(canvas: Canvas) {
        val boardWidth = gameManager.board[0].size
        val boardHeight = gameManager.board.size
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

        legendRight = right
        legendBottom = bottom


        legendCirclePaint.color = ContextCompat.getColor(context, R.color.legend_background)
        legendCirclePaint.style = Paint.Style.FILL
        legendCirclePaint.isAntiAlias = true
        legendCirclePaint.isDither = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // canvas.drawColor(Color.WHITE)
        gameManager.update()
        if (boardLeft != -1) {
            gameManager.drawHighlights(canvas, boardLeft, boardTop, boardRight, boardBottom)
            gameManager.drawBoard(canvas, boardLeft, boardTop, boardRight, boardBottom)
        } else {
            placeBoard(canvas)
        }

        if (legendRight != -1) {
            canvas.drawCircle(0f, 0f, (height / 4).toFloat(), legendCirclePaint)
            gameManager.drawGoal(canvas, 1, 1, legendRight, legendBottom)
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
                gameManager.resetHighlights()
                true
            }
            MotionEvent.ACTION_MOVE -> {
                gameManager.resetHighlights()
                maybeHighlightEdge(eventStartX, eventStartY, event.x, event.y)
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun closeTo(f: Float, target: Double): Boolean {
        return f > target - 0.3 && f < target + 0.3
    }

    private fun isInsideGrid(x : Float, y : Float) : Boolean {
        return (x >= boardLeft && x <= boardRight && y >= boardTop && y <= boardBottom)
    }


    private fun angleToAxis(theta : Float) : Axis? {
        return if (closeTo(theta, 0.0)) {
            Axis.HORIZONTAL
        } else if (closeTo(theta, PI / 2) || closeTo(theta, -PI / 2)) {
            Axis.VERTICAL
        } else {
            // some weirdo diagonal, ignore it.
            null
        }
    }

    private fun distToDirection(dist : Float) : Direction {
        return if (dist < 0) {
            Direction.FORWARD
        } else {
            Direction.BACKWARD
        }
    }

    private fun maybeEnqueueMove(startX: Float, startY: Float, endX: Float, endY: Float) {
        // ignore events that start outside the grid.
        if (!isInsideGrid(startX, startY)) {
            return
        }

        // Compute swipe angle and distance. Ignore very short swipes.
        val hDist = (startX - endX)
        val vDist = (startY - endY)
        if ((hDist * hDist) + (vDist * vDist) < 3000) {
            return
        }
        val theta = atan(vDist / hDist)

        // Compute Axis and Direction of swipe
        val axis = angleToAxis(theta) ?: return
        val direction = distToDirection(if (axis == Axis.HORIZONTAL) { hDist } else { vDist})
        val offset = if (axis == Axis.HORIZONTAL) {
            floor(gameManager.board.size * (startY - boardTop) / (boardBottom - boardTop)).toInt()
        } else {
            floor(gameManager.board[0].size * (startX - boardLeft) / (boardRight - boardLeft)).toInt()
        }

        gameManager.enqueueMove(axis, direction, offset)
    }


    private fun maybeHighlightEdge(startX: Float, startY: Float, endX: Float, endY: Float) {
        // ignore events that start outside the grid
        if (!isInsideGrid(startX, startY)) {
            return
        }

        // Compute swipe angle and direction
        val hDist = (startX - endX)
        val vDist = (startY - endY)
        if ((hDist * hDist) + (vDist * vDist) < 3000) {
            // Ignore extremely short swipes
            return
        }
        val theta = atan(vDist / hDist)

        // Figure out which axis was swiped and in what direction
        val axis = angleToAxis(theta) ?: return
        val direction = distToDirection(if (axis == Axis.HORIZONTAL) { hDist } else { vDist})
        val offset = if (axis == Axis.HORIZONTAL) {
            floor(gameManager.board.size * (startY - boardTop) / (boardBottom - boardTop)).toInt()
        } else {
            floor(gameManager.board[0].size * (startX - boardLeft) / (boardRight - boardLeft)).toInt()
        }

        gameManager.addHighlights(axis, direction, offset)
    }
}