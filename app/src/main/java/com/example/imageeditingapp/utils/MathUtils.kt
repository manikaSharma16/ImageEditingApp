package com.example.imageeditingapp.utils

import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import com.example.imageeditingapp.minCropRectangleHeightWidth
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

object MathUtils {

    // Calculate the offset to move the image wrt to
    fun computeTranslationOffset(sourceX: Float, sourceY: Float, targetX: Float, targetY: Float): Pair<Float, Float> {
        val dx = targetX - sourceX
        val dy = targetY - sourceY
        return dx to dy
    }

    // S: Compute scaling factor of View to Image (to preserve aspect ratio)
    fun calculateImageViewToImageScale(imageRectangle: RectF, viewWidth: Int, viewHeight: Int): Float {
        val ratioWidth = viewWidth.toFloat() / imageRectangle.width()
        val ratioHeight = viewHeight.toFloat() / imageRectangle.height()
        val fitImageWithinImageView = min(ratioWidth, ratioHeight)
        return fitImageWithinImageView
    }

    fun currentUniformScale(mat: Matrix, affineMatrixValues: FloatArray): Float {
        mat.getValues(affineMatrixValues)
        val a = affineMatrixValues[Matrix.MSCALE_X]
        val c = affineMatrixValues[Matrix.MSKEW_Y]
        Log.d("MSCALEX", "a:$a")
        Log.d("MSKEWX", "c:$c")
        return hypot(a, c)
    }

    fun isWithinThreshold(source: Float, destination: Float, threshold: Float): Boolean {
        if( abs(source - destination) < threshold )
            return true
        return false
    }

    fun updateEdgeMaximum(source: Float, change: Float, maxLimit: Float): Float {
        val destination = source + change
        val sourceMaxLimit = maxLimit - minCropRectangleHeightWidth

        if (destination > sourceMaxLimit)
            return sourceMaxLimit
        else
            return destination
    }

    fun updateEdgeAtMinimum(source: Float, change: Float, minLimit: Float): Float {
        val destination = source + change
        val sourceMinLimit = minLimit + minCropRectangleHeightWidth

        if (destination < sourceMinLimit)
            return sourceMinLimit
        else
            return destination
    }
}