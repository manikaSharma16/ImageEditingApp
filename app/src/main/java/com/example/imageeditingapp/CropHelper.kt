package com.example.imageeditingapp

import android.graphics.Canvas
import android.graphics.RectF
import com.example.imageeditingapp.utils.MathUtils.isWithinThreshold
import com.example.imageeditingapp.utils.MathUtils.updateEdgeAtMinimum
import com.example.imageeditingapp.utils.MathUtils.updateEdgeMaximum
import com.example.imageeditingapp.utils.Visuals

class CropHelper(private val baseView: AffineImageTransformView) {

    var cropRectangleControlPointEdge = ACTIVECROPEDGE.NONE
    var tempCropRectangle = RectF()

    // Coloring the rectangle and the outer area
    fun cropRectanglePaint(canvas: Canvas) {
        canvas.drawRect(baseView.cropRectangle, Visuals.edgeColor)

        val outerRects = arrayOf(
            RectF(0f, 0f, baseView.width.toFloat(), baseView.cropRectangle.top),
            RectF(0f, baseView.cropRectangle.bottom, baseView.width.toFloat(), baseView.height.toFloat()),
            RectF(0f, baseView.cropRectangle.top, baseView.cropRectangle.left, baseView.cropRectangle.bottom),
            RectF(baseView.cropRectangle.right, baseView.cropRectangle.top, baseView.width.toFloat(), baseView.cropRectangle.bottom)
        )

        outerRects.forEach { canvas.drawRect(it, Visuals.shadeColor) }
    }

    // Initialize crop rectangle to be of the same size as the image
    fun initializeCropRectangle() {
        val paddingWidth = baseView.imageRectangle.width() * cropRectanglePadding
        val paddingHeight = baseView.imageRectangle.height() * cropRectanglePadding

        baseView.cropRectangle.set(
            baseView.imageRectangle.left + paddingWidth,
            baseView.imageRectangle.top + paddingHeight,
            baseView.imageRectangle.right - paddingWidth,
            baseView.imageRectangle.bottom - paddingHeight
        )

        baseView.cropRectangleInitialized = true
    }

    /*
      getCropRectangleCurrentControlPoint:
          Get the coordinate on the cropRectangle being touched by the finger, if any
     */
    fun getCropRecCurrentCPoint(x: Float, y: Float): ACTIVECROPEDGE {
        if (!baseView.cropRectangleInitialized)
            return ACTIVECROPEDGE.NONE

        val isLeftEdgeTouched = isWithinThreshold(x, baseView.cropRectangle.left, touchTolerance)
        val isRightEdgeTouched = isWithinThreshold(x, baseView.cropRectangle.right, touchTolerance)
        val isTopEdgeTouched = isWithinThreshold(y, baseView.cropRectangle.top, touchTolerance)
        val isBottomEdgeTouched = isWithinThreshold(y, baseView.cropRectangle.bottom, touchTolerance)

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
        baseView.cropRectangle.set(tempCropRectangle)

        when (cropRectangleControlPointEdge) {
            ACTIVECROPEDGE.LEFT ->
                baseView.cropRectangle.left = updateEdgeMaximum(tempCropRectangle.left, dx, tempCropRectangle.right)
            ACTIVECROPEDGE.RIGHT ->
                baseView.cropRectangle.right = updateEdgeAtMinimum(tempCropRectangle.right, dx, tempCropRectangle.left)
            ACTIVECROPEDGE.TOP ->
                baseView.cropRectangle.top = updateEdgeMaximum(tempCropRectangle.top, dy, tempCropRectangle.bottom)
            ACTIVECROPEDGE.BOTTOM ->
                baseView.cropRectangle.bottom = updateEdgeAtMinimum(tempCropRectangle.bottom, dy, tempCropRectangle.top)
            ACTIVECROPEDGE.TOP_LEFT -> {
                baseView.cropRectangle.left = updateEdgeMaximum(tempCropRectangle.left, dx, tempCropRectangle.right)
                baseView.cropRectangle.top = updateEdgeMaximum(tempCropRectangle.top, dy, tempCropRectangle.bottom)
            }
            ACTIVECROPEDGE.TOP_RIGHT -> {
                baseView.cropRectangle.right = updateEdgeAtMinimum(tempCropRectangle.right, dx, tempCropRectangle.left)
                baseView.cropRectangle.top = updateEdgeMaximum(tempCropRectangle.top, dy, tempCropRectangle.bottom)
            }
            ACTIVECROPEDGE.BOTTOM_LEFT -> {
                baseView.cropRectangle.left = updateEdgeMaximum(tempCropRectangle.left, dx, tempCropRectangle.right)
                baseView.cropRectangle.bottom = updateEdgeAtMinimum(tempCropRectangle.bottom, dy, tempCropRectangle.top)
            }
            ACTIVECROPEDGE.BOTTOM_RIGHT -> {
                baseView.cropRectangle.right = updateEdgeAtMinimum(tempCropRectangle.right, dx, tempCropRectangle.left)
                baseView.cropRectangle.bottom = updateEdgeAtMinimum(tempCropRectangle.bottom, dy, tempCropRectangle.top)
            }
            else -> {}
        }

        this.updateCropRectWithImageConstraints()
    }

    /*
    Ensuring the crop rectangle cannot resize beyond the image
        left >= image.left
        top >= image.top
        right <= image.right
        bottom <= image.bottom
     */
    private fun updateCropRectWithImageConstraints() {
        val imgRect = baseView.imageRectangle
        baseView.cropRectangle.set(
            baseView.cropRectangle.left.coerceAtLeast(imgRect.left),
            baseView.cropRectangle.top.coerceAtLeast(imgRect.top),
            baseView.cropRectangle.right.coerceAtMost(imgRect.right),
            baseView.cropRectangle.bottom.coerceAtMost(imgRect.bottom)
        )
    }

    fun resetCropProperties() {
        // isCropModeActive = false
        baseView.cropRectangleInitialized = false
        cropRectangleControlPointEdge = ACTIVECROPEDGE.NONE
        baseView.cropRectangle.setEmpty()
        tempCropRectangle.setEmpty()
    }
}
