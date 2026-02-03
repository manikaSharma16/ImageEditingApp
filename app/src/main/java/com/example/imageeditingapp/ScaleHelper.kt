package com.example.imageeditingapp

import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.example.imageeditingapp.utils.DrawableUtils

class ScaleHelper(
    private val baseView: AffineImageTransformView
) {
    private val scaleDetector = ScaleGestureDetector(baseView.context, ScaleDetectorListener())

    fun onTouchEvent(event: MotionEvent) {
        scaleDetector.onTouchEvent(event)
    }

    /*
    * If crop rectangle is drawn:
    *       Perform temporary scaling
    *       If scaling doesn't covers crop rectangle:
    *           Don't allow scaling
    *           return
    * Perform scaling
    *       Translate the image to minor offset disturbances
    * Update system.imageMatrix
     */
    private inner class ScaleDetectorListener :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {


        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val drawable = baseView.drawable ?: return false

            if (!DrawableUtils.hasImageAndLayout(drawable, baseView.width, baseView.height))
                return false

            val currentScale = detector.scaleFactor
            Log.d("IMAGE", "Current Scale:$currentScale")

            if (baseView.cropRectangleInitialized) {
                val tempAffineMatrix = Matrix()
                val tempImageRectangle = RectF()

                tempAffineMatrix.set(baseView.affineMatrix)
                performScaling(tempAffineMatrix, detector, currentScale)
                tempImageRectangle.set(
                    0f,
                    0f,
                    drawable.intrinsicWidth.toFloat(),
                    drawable.intrinsicHeight.toFloat()
                )
                tempAffineMatrix.mapRect(tempImageRectangle)

                if (!baseView.isImageCoverCropRectangle(tempImageRectangle, baseView.cropRectangle))
                        return true
            }

            performScaling(baseView.affineMatrix, detector, currentScale)
            if (baseView.cropRectangleInitialized)
                baseView.constraintCropRectToImage()

            baseView.imageMatrix.set(baseView.affineMatrix)
            baseView.invalidate()
            baseView.logBounds()

            return true
        }

        /*
        * Scale the image wrt origin and translate back to the source (center between the fingers)
        *
        * 1. Translate to origin
        * 2. Perform scaling
        * 3. Translate to focus point
        */
        private fun performScaling(
            affineMatrix: Matrix,
            detector: ScaleGestureDetector,
            currentScale: Float
        ) {
            affineMatrix.postTranslate(-detector.focusX, -detector.focusY)
            affineMatrix.postScale(currentScale, currentScale)
            affineMatrix.postTranslate(detector.focusX, detector.focusY)
        }
    }
}