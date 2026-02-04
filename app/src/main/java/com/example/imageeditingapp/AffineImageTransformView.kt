package com.example.imageeditingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

/**
 * AffineImageTransformView: Custom ImageView
 *
 * Features added:
 * - Fit image within ImageView
 * - Zoom with Crop rectangle support
 * - Rotate
 * - Resize-able crop rectangle
 * - Pan with Crop rectangle support
 *
 * Features to add:
 * - Rotate with Crop rectangle support
 */
class AffineImageTransformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    val affineMatrix = Matrix() // 3x3 Affine matrix to perform scaling, skewing, translation
    var imageRectangle = RectF() // Image bounds after transformation
    private val affineMatrixValues = FloatArray(9)

    // Crop Variables
    var isCropModeActive = false // To toggle on press of crop button
    var cropRectangle = RectF() // Crop rectangle (reinitialized with every click)
    var cropRectangleInitialized = false // To check if the cropRectangle has been initialized

    private var imageInitializerHelper: ImageInitializerHelper = ImageInitializerHelper(this)
    private var scaleGestureController: ScaleHelper = ScaleHelper(this)
    private var cropController = CropHelper(this)
    private var sharedAffineHelperMethods = SharedAffineHelperMethods(this)
    private var panController = PanHelper(this)

    /*
    override on touch:
    1. Zoom: Allow always
    2. Crop
    3. Pan
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {

        scaleGestureController.onTouchEvent(event) // Delegate all events to ScaleGestureDetector Class

        return if (isCropModeActive) {
            cropController.controlCropAndPan(event)
        } else {
            panController.performPan(event)
        }
    }

    // Draws a new crop rectangle (with a padding) wrt the bounds of the image, and shades the outer area
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isCropModeActive) return

        if (!cropRectangleInitialized) {
            cropController.initializeCropRectangle()
        }

        cropController.cropRectanglePaint(canvas)
    }

    /**
     * API interacting with MainActivity
     */

    fun updateImageRectangle() {
        sharedAffineHelperMethods.updateImageRectangle()
    }

//    fun isImageCoverCropRectangle(
//            imageRectangle: RectF,
//            cropRectangle: RectF
//        ): Boolean {
//            return sharedAffineHelperMethods.isImageCoverCropRectangle(imageRectangle, cropRectangle)
//    }

    fun isImageCoverCropRectangle(
        imageMatrix: Matrix
    ): Boolean {
        return sharedAffineHelperMethods.isImageCoverCropRectangle(imageMatrix)
    }

    fun logBounds() {
        sharedAffineHelperMethods.logBounds()
    }

    fun constraintCropRectToImage() {
        sharedAffineHelperMethods.constraintCropRectToImage()
    }

    fun initializeImage(){
        imageInitializerHelper.initializeImage()
    }

    fun switchCropMode() {
        cropController.switchCropMode()
    }

    fun resetCropProperties() {
        cropController.resetCropProperties()
    }
}