package com.paradise.drowsydetector.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class OvalOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint()

    private val ovalRect = RectF()
    fun getOvalRect() = ovalRect

    private val blurPaint = Paint()
    private val ovalPath = Path()

    init {
        paint.apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 15f
        }

        blurPaint.apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.FILL
            alpha = 128
        }

        blurPaint.apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.FILL
            alpha = 128
        }

    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val centerX = width / 2
        val centerY = height / 2.8
        val radiusX = width / 2.8
        val radiusY = height / 3.4

        ovalRect.set(
            (centerX - radiusX).toFloat(),
            (centerY - radiusY).toFloat(),
            (centerX + radiusX).toFloat(),
            (centerY + radiusY).toFloat()
        )

        // The following code adjusts the outer part of the oval shape to be slightly blurred and the inner part to be transparent.
//        ovalPath.addOval(ovalRect, Path.Direction.CCW)
        ovalPath.addRect(ovalRect, Path.Direction.CCW)
//        if (Build.VERSION.SDK_INT >= 26) {
//            canvas?.clipOutPath(ovalPath)
//        } else {
//            @Suppress("DEPRECATION") canvas?.clipPath(ovalPath, Region.Op.DIFFERENCE)
//        }
        canvas?.clipOutPath(ovalPath)
        canvas?.drawPaint(blurPaint)
    }
}