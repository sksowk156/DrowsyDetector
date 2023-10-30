package com.paradise.drowsydetector.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class OvalOverlayView(context: Context, attrs: AttributeSet?) :
    View(context, attrs) {
    private val paint = Paint()

    private val ovalRect = RectF()
    fun getOvalRect() = ovalRect

    var blurPaint = Paint()

    private val ovalPath = Path()

    init {
        paint.apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 15f
        }

        blurPaint.apply {
            isAntiAlias = true
            color = Color.RED
            style = Paint.Style.FILL
            alpha = 30
        }

    }

    fun onZeroAngle(state:Int) {
        when(state){
            1->{
                blurPaint.apply {
                    isAntiAlias = true
                    color = Color.GREEN
                    style = Paint.Style.FILL
                    alpha = 40
                }
            }
            2->{
                blurPaint.apply {
                    isAntiAlias = true
                    color = Color.BLUE
                    style = Paint.Style.FILL
                    alpha = 40
                }
            }
            3->{
                blurPaint.apply {
                    isAntiAlias = true
                    color = Color.RED
                    style = Paint.Style.FILL
                    alpha = 40
                }
            }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        ovalRect.set(
            (width / 10).toFloat(),
            (height / 10).toFloat(),
            (width * 9 / 10).toFloat(),
            (height * 9 / 10).toFloat()
        )

        ovalPath.addRect(ovalRect, Path.Direction.CCW)
//        if (Build.VERSION.SDK_INT >= 26) {
//            canvas?.clipOutPath(ovalPath)
//        } else {
//            @Suppress("DEPRECATION") canvas?.clipPath(ovalPath, Region.Op.DIFFERENCE)
//        }
        canvas.clipOutPath(ovalPath)
        canvas.drawPaint(blurPaint)
    }

}