package com.matija.imagerec

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.cloudmersive.client.model.Face

class MyImageView(context: Context, attributeSet: AttributeSet) : androidx.appcompat.widget.AppCompatImageView(context, attributeSet) {

    var faces = mutableListOf<Face>()
    var imageWidth = 1
    var imageHeight = 1
    private val paint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val widthRatio = width/imageWidth.toFloat()
        val heightRatio = height/imageHeight.toFloat()
        val points = mutableListOf<Float>()
        faces.forEach { face ->
            val bottomY = heightRatio * face.bottomY
            val topY = heightRatio * face.topY
            val leftX = widthRatio * face.leftX
            val rightX = widthRatio * face.rightX
            points.addAll(arrayOf(leftX, bottomY, rightX, bottomY) )
            points.addAll(arrayOf(rightX, bottomY, rightX, topY) )
            points.addAll(arrayOf(rightX, topY, leftX, topY) )
            points.addAll(arrayOf(leftX, topY, leftX, bottomY) )
        }
        canvas.drawLines(points.toFloatArray(), paint)
    }
}