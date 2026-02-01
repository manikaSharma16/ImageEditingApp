package com.example.imageeditingapp.utils

import android.graphics.Paint
import android.graphics.Color
import com.example.imageeditingapp.cropRectangleWidth

object Visuals {
    val shadeColor = Paint().apply {
        color = Color.parseColor("#88000000")
        style = Paint.Style.FILL
    }

    val edgeColor = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = cropRectangleWidth
    }
}
