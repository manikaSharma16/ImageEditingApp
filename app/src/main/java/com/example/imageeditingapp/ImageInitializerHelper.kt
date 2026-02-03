package com.example.imageeditingapp

import android.graphics.drawable.Drawable
import com.example.imageeditingapp.utils.DrawableUtils
import com.example.imageeditingapp.utils.MathUtils

class ImageInitializerHelper(
    private val baseView: AffineImageTransformView
) {

    fun resetImageVariables(image: Drawable) {
        baseView.affineMatrix.reset()

        val (imageWidth, imageHeight) = DrawableUtils.getImageSize(image) ?: return
        baseView.imageRectangle.set(0f, 0f, imageWidth, imageHeight)
    }

    /*
     * Fit the image within the ImageView and perform centering
     *
     * 1. Initialize affine matrix M = I3
     * 2. Perform scaling
     * 3. Compute translation offset to center image inside view
     * 4. Perform translation to move image wrt center of view
     */
    fun fitImageToView() {
        val scalingFactor = MathUtils.calculateImageViewToImageScale(baseView.imageRectangle, baseView.width, baseView.height)
        baseView.affineMatrix.postScale(scalingFactor, scalingFactor)
        baseView.affineMatrix.mapRect(baseView.imageRectangle)
        baseView.imageMatrix = baseView.affineMatrix

        val targetX = baseView.width / 2f
        val targetY = baseView.height / 2f
        val sourceX = baseView.imageRectangle.width() / 2f
        val sourceY = baseView.imageRectangle.height() / 2f
        val (dx, dy) = MathUtils.computeTranslationOffset(sourceX, sourceY, targetX, targetY)
        baseView.affineMatrix.postTranslate(dx, dy)

        baseView.affineMatrix.mapRect(baseView.imageRectangle)
        baseView.imageMatrix = baseView.affineMatrix
    }
}