package com.example.imageeditingapp

import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log

class SharedAffineHelperMethods(private val baseView: AffineImageTransformView) {

    // To update image rectangle before reading an image rectangle
    fun updateImageRectangle() {
        val drawable = baseView.drawable ?: return
        baseView.imageRectangle.set(
            0f, 0f,
            drawable.intrinsicWidth.toFloat(),
            drawable.intrinsicHeight.toFloat()
        )
        baseView.imageMatrix.mapRect(baseView.imageRectangle)
    }

    // Check if image covers crop rectangle
    fun isImageCoverCropRectangle(
        im: Matrix
    ): Boolean {
        val drawable = baseView.drawable ?: return false

        val inverseMatrix = Matrix()
        if (!im.invert(inverseMatrix))
            return false

        val cropRectangleCorners = floatArrayOf(
            baseView.cropRectangle.left, baseView.cropRectangle.top,
            baseView.cropRectangle.right, baseView.cropRectangle.top,
            baseView.cropRectangle.right, baseView.cropRectangle.bottom,
            baseView.cropRectangle.left, baseView.cropRectangle.bottom
        )

        inverseMatrix.mapPoints(cropRectangleCorners)

        val tempImageCornersWorldSpace = RectF(
            0f, 0f,
            drawable.intrinsicWidth.toFloat(),
            drawable.intrinsicHeight.toFloat()
        )

        for (i in cropRectangleCorners.indices step 2) {
            val x = cropRectangleCorners[i]
            val y = cropRectangleCorners[i + 1]

            if (!tempImageCornersWorldSpace.contains(x, y))
                return false
        }

        return true
    }

//    fun isImageCoverCropRectangle(
//        imageRectangle: RectF,
//        cropRectangle: RectF
//    ): Boolean {
//
//        baseView.updateImageRectangle()
//
//        return imageRectangle.left <= cropRectangle.left &&
//                imageRectangle.top <= cropRectangle.top &&
//                imageRectangle.right >= cropRectangle.right &&
//                imageRectangle.bottom >= cropRectangle.bottom
//    }

    fun logBounds() {
        Log.d("IMAGEVIEWBOUNDS", "L:$baseView.left T:$baseView.top R:$baseView.right B:$baseView.bottom")
        Log.d("IMAGEBOUNDS", "W:${baseView.imageRectangle.width()}, H:${baseView.imageRectangle.height()},L:${baseView.imageRectangle.left} T:${baseView.imageRectangle.top} R:${baseView.imageRectangle.right} B:${baseView.imageRectangle.bottom}")
    }

    // Translate the image if error between image and crop rectangle
    fun constraintCropRectToImage() {
        updateImageRectangle()

        val dx = when {
            baseView.imageRectangle.left > baseView.cropRectangle.left -> baseView.cropRectangle.left - baseView.cropRectangle.left
            baseView.imageRectangle.right < baseView.cropRectangle.right -> baseView.cropRectangle.right - baseView.cropRectangle.right
            else -> 0f
        }

        val dy = when {
            baseView.imageRectangle.top > baseView.cropRectangle.top -> baseView.cropRectangle.top - baseView.cropRectangle.top
            baseView.imageRectangle.bottom < baseView.cropRectangle.bottom -> baseView.cropRectangle.bottom - baseView.cropRectangle.bottom
            else -> 0f
        }

        if (dx == 0f && dy == 0f)
            return

        baseView.imageMatrix.postTranslate(dx, dy)
    }
}