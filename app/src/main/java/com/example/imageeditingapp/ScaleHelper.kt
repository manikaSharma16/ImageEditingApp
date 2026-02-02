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
    * Scale the image wrt origin and translate back to the source (center between the fingers)
    *
    * 1. Translate to origin
    * 2. Perform scaling
    * 3. Translate to focus point
    */
    private inner class ScaleDetectorListener :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {


        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val drawable = baseView.drawable ?: return false

            if (!DrawableUtils.hasImageAndLayout(drawable, baseView.width, baseView.height))
                return false

            val currentScale = detector.scaleFactor
            Log.d("IMAGE", "Current Scale:$currentScale")

            if (baseView.isCropModeActive && baseView.cropRectangleInitialized) {
                val temp = Matrix(baseView.imageMatrix)
                temp.postTranslate(-detector.focusX, -detector.focusY)
                temp.postScale(currentScale, currentScale)
                temp.postTranslate(detector.focusX, detector.focusY)

                val tempImageRect = RectF(
                    0f, 0f,
                    drawable.intrinsicWidth.toFloat(),
                    drawable.intrinsicHeight.toFloat()
                )
                temp.mapRect(tempImageRect)

                if (!isImageCoverCropRect(tempImageRect, baseView.cropRectangle) && currentScale < 1f) {
                    return true
                }
            }

            performScaling(detector, currentScale)

            if (baseView.isCropModeActive && baseView.cropRectangleInitialized) {
                baseView.constraintCropRectToImage()
            }

            baseView.updateImageRectangle()
            baseView.logBounds()

            baseView.invalidate()
            return true
        }

        private fun performScaling(detector: ScaleGestureDetector, currentScale: Float) {
            baseView.affineMatrix.set(baseView.imageMatrix)

            baseView.affineMatrix.postTranslate(-detector.focusX, -detector.focusY)
            baseView.affineMatrix.postScale(currentScale, currentScale)
            baseView.affineMatrix.postTranslate(detector.focusX, detector.focusY)

            baseView.applyAffineTranformOnImage()
        }

        private fun isImageCoverCropRect(imageRect: RectF, cropRect: RectF): Boolean {
            return imageRect.left <= cropRect.left &&
                    imageRect.top <= cropRect.top &&
                    imageRect.right >= cropRect.right &&
                    imageRect.bottom >= cropRect.bottom
        }
    }
}