package com.example.imageeditingapp.math

import android.graphics.Matrix
import android.graphics.RectF
import kotlin.math.min

object MathUtils {

    // Calculate the offset to move the image wrt to
    fun computeTranslationOffset(imageRectangle: RectF, targetX: Float, targetY: Float): Pair<Float, Float> {
        val currentX = (imageRectangle.left + imageRectangle.right) / 2f
        val currentY = (imageRectangle.top + imageRectangle.bottom) / 2f

        val dx = targetX - currentX
        val dy = targetY - currentY
        return dx to dy
    }

    // S: Compute scaling factor of View to Image (to preserve aspect ratio)
    fun calculateImageViewToImageScale(imageRectangle: RectF, viewWidth: Int, viewHeight: Int): Float {
        val ratioWidth = viewWidth.toFloat() / imageRectangle.width()
        val ratioHeight = viewHeight.toFloat() / imageRectangle.height()
        val fitImageWithinImageView = min(ratioWidth, ratioHeight)
        return fitImageWithinImageView
    }

    // Perform scaling
    fun scale(affineMatrix: Matrix, imageRectangle: RectF, viewWidth: Int, viewHeight: Int) {
        val scalingFactor = calculateImageViewToImageScale(imageRectangle, viewWidth, viewHeight)
        affineMatrix.postScale(scalingFactor, scalingFactor)
        affineMatrix.mapRect(imageRectangle)
    }

    // Perform Translation
    fun translate(affineMatrix: Matrix, imageRectangle: RectF, dx: Float, dy: Float) {
        affineMatrix.postTranslate(dx, dy)
        affineMatrix.mapRect(imageRectangle)
    }

}