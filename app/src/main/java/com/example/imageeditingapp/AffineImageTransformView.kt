package com.example.imageeditingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatImageView
import com.example.imageeditingapp.utils.DrawableUtils
import com.example.imageeditingapp.utils.MathUtils
import com.example.imageeditingapp.utils.MathUtils.isWithinThreshold
import com.example.imageeditingapp.utils.MathUtils.updateEdgeAtMinimum
import com.example.imageeditingapp.utils.MathUtils.updateEdgeMaximum
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
    private var cropRectangleInitialized = false // To check if the cropRectangle has been initialized
    private var cropRectangleControlPointEdge = ACTIVECROPEDGE.NONE // The current edge to resize from
    private var curTouchX = 0f // Current coordinates by touch
    private var curTouchY = 0f
    private var tempCropRectangle = RectF()

    /*
    override on touch:
    1. Zoom: Allow always
    2. Crop:
        2.a: On touch: If the finger touches the edge of crop rectangle, get the control point edge
        2.b: On move: If the finger moves, resize the crop rectangle from the control point edge
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {

        scaleDetector.onTouchEvent(event) // Delegate all events to ScaleGestureDetector Class

        if (!isCropModeActive) return true

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> { // On-Finger-Touch
                cropRectangleControlPointEdge = Crop().getCropRecCurrentCPoint(event.x, event.y)
                if (cropRectangleControlPointEdge != ACTIVECROPEDGE.NONE) {
                    curTouchX = event.x
                    curTouchY = event.y
                    tempCropRectangle.set(cropRectangle)
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (cropRectangleControlPointEdge != ACTIVECROPEDGE.NONE) { // On-Finger-Move
                    val dx = event.x - curTouchX
                    val dy = event.y - curTouchY
                    Crop().resizeCropRectangle(dx, dy)
                    invalidate()
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                cropRectangleControlPointEdge = ACTIVECROPEDGE.NONE
            }
        }

        return true
    }

    // Toggle crop mode and redraw the canvas
    fun switchCropMode() {
        isCropModeActive = !isCropModeActive

        if (!isCropModeActive) {
            cropRectangleInitialized = false
            cropRectangleControlPointEdge = ACTIVECROPEDGE.NONE
        }

        invalidate()
    }

    // Draws a new crop rectangle (with a padding) wrt the bounds of the image, and shades the outer area
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isCropModeActive) return

        updateImageRectangle()

        if (!cropRectangleInitialized) {
            Crop().initializeCropRectangle()
        }

        Crop().cropRectanglePaint(cropRectangle, canvas)
    }

    // To update image rectangle after every operation
    private fun updateImageRectangle() {
        val drawable = drawable ?: return
        val temp = RectF(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        imageMatrix.mapRect(temp)
        imageRectangle.set(temp)
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

    inner class Crop{

        // Coloring the rectangle and the outer area
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

        // Initialize crop rectangle to be of the same size as the image
        fun initializeCropRectangle() {
            val paddingWidth = imageRectangle.width() * cropRectanglePadding
            val paddingHeight = imageRectangle.height() * cropRectanglePadding

            cropRectangle.set(
                imageRectangle.left + paddingWidth,
                imageRectangle.top + paddingHeight,
                imageRectangle.right - paddingWidth,
                imageRectangle.bottom - paddingHeight
            )

            cropRectangleInitialized = true
        }

        /*
        getCropRectangleCurrentControlPoint:
            Get the coordinate on the cropRectangle being touched by the finger, if any
         */
        fun getCropRecCurrentCPoint(x: Float, y: Float): ACTIVECROPEDGE {

            if (!cropRectangleInitialized)
                return ACTIVECROPEDGE.NONE

            val isLeftEdgeTouched = isWithinThreshold(x, cropRectangle.left, touchTolerance)
            val isRightEdgeTouched = isWithinThreshold(x, cropRectangle.right, touchTolerance)
            val isTopEdgeTouched = isWithinThreshold(y, cropRectangle.top, touchTolerance)
            val isBottomEdgeTouched = isWithinThreshold(y, cropRectangle.bottom, touchTolerance)

            return when {
                isTopEdgeTouched && isLeftEdgeTouched -> ACTIVECROPEDGE.TOP_LEFT
                isTopEdgeTouched && isRightEdgeTouched -> ACTIVECROPEDGE.TOP_RIGHT
                isBottomEdgeTouched && isLeftEdgeTouched -> ACTIVECROPEDGE.BOTTOM_LEFT
                isBottomEdgeTouched && isRightEdgeTouched -> ACTIVECROPEDGE.BOTTOM_RIGHT
                isTopEdgeTouched -> ACTIVECROPEDGE.TOP
                isBottomEdgeTouched -> ACTIVECROPEDGE.BOTTOM
                isLeftEdgeTouched -> ACTIVECROPEDGE.LEFT
                isRightEdgeTouched -> ACTIVECROPEDGE.RIGHT
                else -> ACTIVECROPEDGE.NONE
            }
        }

        /*
        resizeCropRectangle:
            resize the crop rectangle from the current selected edge, while respecting the bounds
            left <= right - minWidth
            right >= left + minWidth
            top <= bottom - minHeight
            bottom >= top + minHeight
         */
        fun resizeCropRectangle(dx: Float, dy: Float) {

            cropRectangle.set(tempCropRectangle)

            when (cropRectangleControlPointEdge) {

                ACTIVECROPEDGE.LEFT ->
                    cropRectangle.left = updateEdgeMaximum(tempCropRectangle.left, dx, tempCropRectangle.right)

                ACTIVECROPEDGE.RIGHT ->
                    cropRectangle.right = updateEdgeAtMinimum(tempCropRectangle.right, dx, tempCropRectangle.left)

                ACTIVECROPEDGE.TOP ->
                    cropRectangle.top = updateEdgeMaximum(tempCropRectangle.top, dy, tempCropRectangle.bottom)

                ACTIVECROPEDGE.BOTTOM ->
                    cropRectangle.bottom = updateEdgeAtMinimum(tempCropRectangle.bottom, dy, tempCropRectangle.top)

                ACTIVECROPEDGE.TOP_LEFT -> {
                    cropRectangle.left = updateEdgeMaximum(tempCropRectangle.left, dx, tempCropRectangle.right)
                    cropRectangle.top = updateEdgeMaximum(tempCropRectangle.top, dy, tempCropRectangle.bottom)
                }

                ACTIVECROPEDGE.TOP_RIGHT -> {
                    cropRectangle.right = updateEdgeAtMinimum(tempCropRectangle.right, dx, tempCropRectangle.left)
                    cropRectangle.top = updateEdgeMaximum(tempCropRectangle.top, dy, tempCropRectangle.bottom)
                }

                ACTIVECROPEDGE.BOTTOM_LEFT -> {
                    cropRectangle.left = updateEdgeMaximum(tempCropRectangle.left, dx, tempCropRectangle.right)
                    cropRectangle.bottom = updateEdgeAtMinimum(tempCropRectangle.bottom, dy, tempCropRectangle.top)
                }

                ACTIVECROPEDGE.BOTTOM_RIGHT -> {
                    cropRectangle.right = updateEdgeAtMinimum(tempCropRectangle.right, dx, tempCropRectangle.left)
                    cropRectangle.bottom = updateEdgeAtMinimum(tempCropRectangle.bottom, dy, tempCropRectangle.top)
                }

                else -> {}
            }

            updateCropRectWithImageConstraints()
        }

        /*
        Ensuring the crop rectangle cannot resize beyond the image
            left >= image.left
            top >= image.top
            right <= image.right
            bottom <= image.bottom
         */
        private fun updateCropRectWithImageConstraints() {
            cropRectangle.set(
                cropRectangle.left.coerceAtLeast(imageRectangle.left),
                cropRectangle.top.coerceAtLeast(imageRectangle.top),
                cropRectangle.right.coerceAtMost(imageRectangle.right),
                cropRectangle.bottom.coerceAtMost(imageRectangle.bottom)
            )
        }
    }
}