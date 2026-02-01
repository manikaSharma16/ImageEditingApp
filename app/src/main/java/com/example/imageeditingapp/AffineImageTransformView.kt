package com.example.imageeditingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.ScaleGestureDetector
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatImageView
import com.example.imageeditingapp.utils.DrawableUtils
import com.example.imageeditingapp.utils.MathUtils
import com.example.imageeditingapp.utils.Visuals

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

    private var isCropModeActive = false // To toggle on press of crop button
    private var cropRectangle = RectF() // Crop rectangle (reinitialized with every click)

    // Toggle crop mode and redraw the canvas
    fun switchCropMode() {
        isCropModeActive = !isCropModeActive
        invalidate()
    }

    // Draws a new crop rectangle (with a padding) wrt the bounds of the image, and shades the outer area
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isCropModeActive) return

        updateImageRectangle()

        val paddingWidth = imageRectangle.width() * cropRectanglePadding
        val paddingHeight = imageRectangle.height() * cropRectanglePadding
        cropRectangle.set(
            imageRectangle.left + paddingWidth,
            imageRectangle.top + paddingHeight,
            imageRectangle.right - paddingWidth,
            imageRectangle.bottom - paddingHeight
        )

        Crop().cropRectanglePaint(cropRectangle, canvas)
    }

    // To update image rectangle after every operation
    private fun updateImageRectangle() {
        val drawable = drawable ?: return
        val temp = RectF(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        imageMatrix.mapRect(temp)
        imageRectangle.set(temp)
    }

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

        if (!DrawableUtils.hasImageAndLayout(image, width, height))
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

    private inner class ScaleDetectorListener :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {

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

            performScaling(detector, currentScale)
            updateImageRectangle()

            logBounds()
            return true
        }

        private fun performScaling(detector: ScaleGestureDetector, currentScale: Float) {

            affineMatrix.set(imageMatrix)
            affineMatrix.postTranslate(-detector.focusX, -detector.focusY)
            affineMatrix.postScale(currentScale, currentScale)
            affineMatrix.postTranslate(detector.focusX, detector.focusY)

            affineMatrix.mapRect(imageRectangle)
            imageMatrix = affineMatrix
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

                performRotation(deltaRotation)
                updateImageRectangle()
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        fun performRotation(rotationAngle: Float) {
            val sourceX = width / 2f
            val sourceY = height / 2f

            affineMatrix.set(imageMatrix)
            affineMatrix.postTranslate(-sourceX, -sourceY)
            affineMatrix.postRotate(rotationAngle)
            affineMatrix.postTranslate(sourceX, sourceY)

            affineMatrix.mapRect(imageRectangle)
            imageMatrix = affineMatrix
        }
    }

    /*
    On draw functionalities of crop rectangle:
        1. Coloring the rectangle and the outer area
     */
    inner class Crop{
        fun cropRectanglePaint(cropRectangle: RectF, canvas: Canvas) {

            canvas.drawRect(cropRectangle, Visuals.edgeColor)

            val outerRectangles = arrayOf(
                RectF(0f, 0f, width.toFloat(), cropRectangle.top),
                RectF(0f, cropRectangle.bottom, width.toFloat(), height.toFloat()),
                RectF(0f, cropRectangle.top, cropRectangle.left, cropRectangle.bottom),
                RectF(cropRectangle.right, cropRectangle.top, width.toFloat(), cropRectangle.bottom)
            )

            outerRectangles.forEach { rectangle ->
                canvas.drawRect(rectangle, Visuals.shadeColor)
            }
        }

    }
}