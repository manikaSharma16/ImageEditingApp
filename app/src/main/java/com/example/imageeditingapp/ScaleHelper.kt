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
                val tempImageMatrix = Matrix()
                val tempImageRectangle = RectF()

                tempImageMatrix.set(baseView.imageMatrix)
                performScaling(tempImageMatrix, detector, currentScale)
                tempImageRectangle.set(
                    0f,
                    0f,
                    drawable.intrinsicWidth.toFloat(),
                    drawable.intrinsicHeight.toFloat()
                )
                tempImageMatrix.mapRect(tempImageRectangle)

                if (!baseView.isImageCoverCropRectangle(tempImageRectangle, baseView.cropRectangle))
                        return true
            }

            performScaling(baseView.imageMatrix, detector, currentScale)
            if (baseView.cropRectangleInitialized)
                baseView.constraintCropRectToImage()

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
            imageMatrix: Matrix,
            detector: ScaleGestureDetector,
            currentScale: Float
        ) {
            imageMatrix.postTranslate(-detector.focusX, -detector.focusY)
            imageMatrix.postScale(currentScale, currentScale)
            imageMatrix.postTranslate(detector.focusX, detector.focusY)
        }
    }
}