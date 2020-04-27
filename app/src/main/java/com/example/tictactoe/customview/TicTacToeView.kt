package com.example.tictactoe.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Path
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.tictactoe.customview.constant.State
import com.example.tictactoe.customview.instancestate.TicTacToeInstanceState
import com.example.tictactoe.toBoolean
import com.example.tictactoe.toInt

class TicTacToeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var boardSize = 300
    private var userOddTouchFlag = false
    private val boardList by lazy {
        mutableListOf<Rect>()
    }
    private val boardStateList by lazy {
        mutableListOf<String>()
    }
    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }
    private val path by lazy {
        Path()
    }

    init {
        isSaveEnabled = true
        setupPaint()
        for (index in 1..9) {
            boardStateList.add(State.BLANK)
        }
    }

    private fun setupPaint() {
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        setupStrokeWidth()
    }

    private fun setupStrokeWidth() {
        val displayMetrics = context.resources.displayMetrics
        val density = displayMetrics.density
        paint.strokeWidth = density * DEFAULT_STROKE_WIDTH
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = widthMeasureSpec.coerceAtMost(heightMeasureSpec)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = w.coerceAtMost(h)
        boardSize = size / 3
        this.generateBoard()
    }

    private fun generateBoard() {
        var row = 0
        for (index in 1..9) {
            var column = (index % 3) - 1
            if (column < 0) {
                column = 2
            }
            val rect = Rect()
            with(rect) {
                top = row * boardSize
                left = column * boardSize
                right = left + boardSize
                bottom = top + boardSize
            }
            boardList.add(rect)
            if (index % 3 == 0) {
                row = index / 3
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { it ->
            drawBoard(it)
            boardList.forEachIndexed { index, board ->
                when (boardStateList[index]) {
                    State.CIRCLE -> drawCircle(canvas, board)
                    State.CROSS -> drawCross(canvas, board)
                }
            }
        }
    }

    private fun drawBoard(canvas: Canvas) {
        boardList.forEach {
            canvas.drawRect(it, paint)
        }
    }

    private fun drawCircle(canvas: Canvas, block: Rect) {
        canvas.drawCircle(
            block.exactCenterX(),
            block.exactCenterY(),
            boardSize.toFloat() / 3,
            paint
        )
    }

    private fun drawCross(canvas: Canvas, block: Rect) {
        path.moveTo(block.left.toFloat() + CROSS_OFFSET, block.top.toFloat() + CROSS_OFFSET)
        path.lineTo(block.right.toFloat() - CROSS_OFFSET, block.bottom.toFloat() - CROSS_OFFSET)
        path.moveTo(block.right.toFloat() - CROSS_OFFSET, block.top.toFloat() + CROSS_OFFSET)
        path.lineTo(block.left.toFloat() + CROSS_OFFSET, block.bottom.toFloat() - CROSS_OFFSET)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> updateBoardState(event)
                else -> true
            }
        }
        return true
    }

    private fun updateBoardState(event: MotionEvent): Boolean {
        boardList.asSequence().find { rect ->
            rect.contains(event.x.toInt(), event.y.toInt())
        }?.apply {
            val index = boardList.indexOf(this)
            if (boardStateList[index] != State.BLANK) return true
            if (userOddTouchFlag) {
                boardStateList[index] = State.CROSS
            } else {
                boardStateList[index] = State.CIRCLE
            }
            userOddTouchFlag = userOddTouchFlag.not()
            invalidate()
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable? {
        val instanceState = super.onSaveInstanceState()
        val state =
            TicTacToeInstanceState(
                instanceState
            )
        state.boardStateList.addAll(this.boardStateList)
        state.userOddTouchFlag = this.userOddTouchFlag.toInt()
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        state?.let {
            val instanceState = it as TicTacToeInstanceState
            super.onRestoreInstanceState(instanceState.superState)
            this.boardStateList.clear()
            this.boardStateList.addAll(instanceState.boardStateList)
            this.userOddTouchFlag = instanceState.userOddTouchFlag.toBoolean()
            invalidate()
        }
    }

    companion object {
        private const val CROSS_OFFSET = 75
        private const val DEFAULT_STROKE_WIDTH = 4F
    }
}