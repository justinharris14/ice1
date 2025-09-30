package com.example.snake2025


import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random


class GameView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {


    enum class Direction { UP, RIGHT, DOWN, LEFT }


    private val threadLock = Object()
    @Volatile private var running = false
    private var thread: Thread? = null


    var onGameOver: ((score: Int) -> Unit)? = null


    private val paint = Paint().apply { isAntiAlias = true }
    private val bgPaint = Paint()