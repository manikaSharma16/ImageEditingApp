package com.example.imageeditingapp

import Utility.DrawableUtils
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import com.example.imageeditingapp.math.MathUtils

/**
 * AffineImageTransformView: Custom ImageView
 *
 * Features added:
 * - Fit image within ImageView
 *
 * Features to add:
 * - Zoom
 * - Pan
 * - Rotate
 */
class AffineImageTransformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val affineMatrix = Matrix() // 3x3 Affine matrix to perform scaling, skewing, translation
    private val imageRectangle = RectF() // Image bounds after transformation
    private val affineMatrixValues = FloatArray(9)

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

        MathUtils.scale(affineMatrix, imageRectangle, width, height)

        val targetX = width / 2f
        val targetY = height / 2f
        val (dx, dy) = MathUtils.computeTranslationOffset(imageRectangle, targetX, targetY)
        MathUtils.translate(affineMatrix, imageRectangle, dx, dy)

        imageMatrix = affineMatrix
        logBounds()
    }

    private fun logBounds() {
        Log.d("IMAGEVIEWBOUNDS", "L:$left T:$top R:$right B:$bottom")
        Log.d("IMAGEBOUNDS", "L:${imageRectangle.left} T:${imageRectangle.top} R:${imageRectangle.right} B:${imageRectangle.bottom}")
    }

}