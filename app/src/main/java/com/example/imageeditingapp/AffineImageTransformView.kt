package com.example.imageeditingapp

import Utility.DrawableUtils
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.ScaleGestureDetector
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatImageView
import com.example.imageeditingapp.math.MathUtils

/**
 * AffineImageTransformView: Custom ImageView
 *
 * Features added:
 * - Fit image within ImageView
 * - Zoom
 *
 * Features to add:
 * - Pan
 * - Rotate
 */
class AffineImageTransformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val affineMatrix = Matrix() // 3x3 Affine matrix to perform scaling, skewing, translation
    var imageRectangle = RectF() // Image bounds after transformation
    private val affineMatrixValues = FloatArray(9)

    private val scaleDetector = ScaleGestureDetector(context, ScaleDetectorListener())
    private var currentRotationAngle = 0f

    init {
        setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            true
        } // Delegate all events to ScaleGestureDtector Class
    }

    /*
     * Fit the image within the ImageView and perform centering
     *
     * 1. Initialize affine matrix M = I3
     * 2. Perform scaling
     * 3. Compute translation offset to center image inside view
     * 4. Perform translation to move image wrt center of view
     */
    fun normalize() {
        val image = drawable

        if(!DrawableUtils.hasImageAndLayout(image, width, height))
            return

        val (imageWidth, imageHeight) = DrawableUtils.getImageSize(image) ?: return

        affineMatrix.reset()
        imageRectangle.set(0f, 0f, imageWidth, imageHeight)

        val scalingFactor = MathUtils.calculateImageViewToImageScale(imageRectangle, width, height)
        affineMatrix.postScale(scalingFactor, scalingFactor)
        affineMatrix.mapRect(imageRectangle)

        val targetX = width / 2f
        val targetY = height / 2f
        val sourceX = imageRectangle.width() / 2f
        val sourceY = imageRectangle.height() / 2f
        val (dx, dy) = MathUtils.computeTranslationOffset(sourceX, sourceY, targetX, targetY)
        affineMatrix.postTranslate(dx, dy)
        affineMatrix.mapRect(imageRectangle)

        imageMatrix = affineMatrix
        logBounds()
    }

    private fun logBounds() {
        Log.d("IMAGEVIEWBOUNDS", "L:$left T:$top R:$right B:$bottom")
        Log.d("IMAGEBOUNDS", "W:${imageRectangle.width()}, H:${imageRectangle.height()},L:${imageRectangle.left} T:${imageRectangle.top} R:${imageRectangle.right} B:${imageRectangle.bottom}")
    }

    private inner class ScaleDetectorListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        /*
        * Scale the image wrt origin and translate back to the source (center between the fingers)
        *
        * 1. Translate to origin
        * 2. Perform scaling
        * 3. Translate to focus point
         */
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (!DrawableUtils.hasImageAndLayout(drawable, width, height))
                return false

            val currentScale = detector.scaleFactor
            Log.d("IMAGE", "Current Scale:$currentScale")

            affineMatrix.set(imageMatrix)

            affineMatrix.postTranslate(-detector.focusX, -detector.focusY)
            affineMatrix.postScale(currentScale, currentScale)
            affineMatrix.postTranslate(detector.focusX, detector.focusY)

            affineMatrix.mapRect(imageRectangle)

            imageMatrix = affineMatrix

            logBounds()
            return true
        }
    }

    inner class RotationSliderListener : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(
            seekBar: SeekBar?,
            rotationAngle: Int,
            fromUser: Boolean
        ) {
            if (fromUser) {
                if (!DrawableUtils.hasImageAndLayout(drawable, width, height))
                    return

                val destinationRotationAngle = rotationAngle.toFloat()
                val deltaRotation = destinationRotationAngle - currentRotationAngle
                currentRotationAngle = destinationRotationAngle

                affineMatrix.set(imageMatrix)

                val sourceX = width / 2f
                val sourceY = height / 2f
                affineMatrix.postTranslate(-sourceX, -sourceY)
                affineMatrix.postRotate(deltaRotation)
                affineMatrix.postTranslate(sourceX, sourceY)

                affineMatrix.mapRect(imageRectangle)
                imageMatrix = affineMatrix
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }
}