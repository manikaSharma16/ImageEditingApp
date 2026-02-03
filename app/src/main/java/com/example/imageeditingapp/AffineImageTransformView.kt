package com.example.imageeditingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.example.imageeditingapp.utils.DrawableUtils

/**
 * AffineImageTransformView: Custom ImageView
 *
 * Features added:
 * - Fit image within ImageView
 * - Zoom with Crop rectangle support
 * - Rotate
 * - Resize-able crop rectangle
 *
 * Features to add:
 * - Pan
 * - Pan with Crop rectangle support
 * - Rotate with Crop rectangle support
 */
class AffineImageTransformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    val affineMatrix = Matrix() // 3x3 Affine matrix to perform scaling, skewing, translation
    var imageRectangle = RectF() // Image bounds after transformation
    private val affineMatrixValues = FloatArray(9)

    var curTouchX = 0f // Current coordinates by touch
    var curTouchY = 0f

    // Crop Variables
    var isCropModeActive = false // To toggle on press of crop button
    var cropRectangle = RectF() // Crop rectangle (reinitialized with every click)
    var cropRectangleInitialized = false // To check if the cropRectangle has been initialized

    private var imageInitializerHelper: ImageInitializerHelper = ImageInitializerHelper(this)
    private var scaleGestureController: ScaleHelper = ScaleHelper(this)
    private var cropController = CropHelper(this)

    /**
     * API interacting with MainActivity
     */
    /*
    initializeImage: Initialize variables
        1. reset image variables:
                1.a: affine matrix = I3
                2.a: image rectangle: 4 corner bound of image
        2. fit image within the view
        3. reset crop variables
     */
    fun initializeImage() {
        val image = drawable
        if (!DrawableUtils.hasImageAndLayout(image, width, height))
            return

        imageInitializerHelper.resetImageVariables(image)
        imageInitializerHelper.fitImageToView()
        cropController.resetCropProperties()

        logBounds()
    }

    // Toggle crop mode and redraw the canvas
    fun switchCropMode() {
        isCropModeActive = !isCropModeActive

        if (!isCropModeActive) {
            cropController.resetCropProperties()
        }

        invalidate()
    }

    /*
    override on touch:
    1. Zoom: Allow always
    2. Crop:
        2.a: On touch: If the finger touches the edge of crop rectangle, get the control point edge
        2.b: On move: If the finger moves, resize the crop rectangle from the control point edge
        2.c: On lift: If finger lifted, redraw the canvas
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {

        scaleGestureController.onTouchEvent(event) // Delegate all events to ScaleGestureDetector Class

        if (!isCropModeActive) return true

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> { // On-Finger-Touch
                cropController.cropRectangleControlPointEdge = cropController.getCropRecCurrentCPoint(event.x, event.y)
                if (cropController.cropRectangleControlPointEdge != ACTIVECROPEDGE.NONE) {
                    curTouchX = event.x
                    curTouchY = event.y
                    cropController.tempCropRectangle.set(cropRectangle)
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (cropController.cropRectangleControlPointEdge != ACTIVECROPEDGE.NONE) { // On-Finger-Move
                    val dx = event.x - curTouchX
                    val dy = event.y - curTouchY
                    cropController.resizeCropRectangle(dx, dy)
                    invalidate()
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { // On-Finger-Interaction-End
                cropController.cropRectangleControlPointEdge = ACTIVECROPEDGE.NONE
                if (cropRectangleInitialized) {
                    invalidate()
                }
            }
        }

        return true
    }

    // Draws a new crop rectangle (with a padding) wrt the bounds of the image, and shades the outer area
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isCropModeActive) return

        updateImageRectangle()

        if (!cropRectangleInitialized) {
            cropController.initializeCropRectangle()
        }

        cropController.cropRectanglePaint(canvas)
    }

    // To update image rectangle after every operation
    fun updateImageRectangle() {
        val drawable = drawable ?: return
        imageRectangle.set(
            0f, 0f,
            drawable.intrinsicWidth.toFloat(),
            drawable.intrinsicHeight.toFloat()
        )
        imageMatrix.mapRect(imageRectangle)
    }

    fun applyAffineTranformOnImage() {
        affineMatrix.mapRect(imageRectangle)
        imageMatrix = affineMatrix
    }

    fun isImageCoverCropRectangle(
        imageRectangle: RectF,
        cropRectangle: RectF
    ): Boolean {
        return imageRectangle.left <= cropRectangle.left &&
                imageRectangle.top <= cropRectangle.top &&
                imageRectangle.right >= cropRectangle.right &&
                imageRectangle.bottom >= cropRectangle.bottom
    }

    fun logBounds() {
        Log.d("IMAGEVIEWBOUNDS", "L:$left T:$top R:$right B:$bottom")
        Log.d("IMAGEBOUNDS", "W:${imageRectangle.width()}, H:${imageRectangle.height()},L:${imageRectangle.left} T:${imageRectangle.top} R:${imageRectangle.right} B:${imageRectangle.bottom}")
    }
    fun constraintCropRectToImage() {
        updateImageRectangle()

        val dx = when {
            imageRectangle.left > cropRectangle.left -> cropRectangle.left - imageRectangle.left
            imageRectangle.right < cropRectangle.right -> cropRectangle.right - imageRectangle.right
            else -> 0f
        }

        val dy = when {
            imageRectangle.top > cropRectangle.top -> cropRectangle.top - imageRectangle.top
            imageRectangle.bottom < cropRectangle.bottom -> cropRectangle.bottom - imageRectangle.bottom
            else -> 0f
        }

        if (dx == 0f && dy == 0f)
            return

        affineMatrix.set(imageMatrix)
        affineMatrix.postTranslate(dx, dy)
    }
}