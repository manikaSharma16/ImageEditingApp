package com.example.imageeditingapp

import android.graphics.Matrix
import android.widget.SeekBar
import com.example.imageeditingapp.utils.DrawableUtils

class RotationHelper(
    private val baseView: AffineImageTransformView,
    private val seekBar: SeekBar
) {

    private var currentRotationAngle = 0f

    init {
        setupSeekBarListener()
    }

    fun performRotation(rotationAngle: Float) {
        val sourceX = baseView.width / 2f
        val sourceY = baseView.height / 2f

        baseView.imageMatrix.postTranslate(-sourceX, -sourceY)
        baseView.imageMatrix.postRotate(rotationAngle)
        baseView.imageMatrix.postTranslate(sourceX, sourceY)

        if (baseView.isCropModeActive) {
            scaleImageToCoverCrop()
        }

        baseView.invalidate()
    }

    fun canRotate(rotationAngle: Float, sourceX: Float, sourceY: Float): Boolean {
        val tempImageMatrix = Matrix()
        tempImageMatrix.set(baseView.imageMatrix)
        tempImageMatrix.postTranslate(-sourceX, -sourceY)
        tempImageMatrix.postRotate(rotationAngle)
        tempImageMatrix.postTranslate(sourceX, sourceY)

        return baseView.isImageCoverCropRectangle(tempImageMatrix)
    }

    private fun scaleImageToCoverCrop() {
        if (baseView.isImageCoverCropRectangle(baseView.imageMatrix))
            return

        val centerX = baseView.width / 2f
        val centerY = baseView.height / 2f

        for (i in 0 until maxScaleIterations) {
            if (baseView.isImageCoverCropRectangle(baseView.imageMatrix))
                break

            baseView.imageMatrix.postScale(scaleStep, scaleStep, centerX, centerY)
        }
    }

    fun setupSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, rotationAngle: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (!DrawableUtils.hasImageAndLayout(baseView.drawable, baseView.width, baseView.height))
                        return
                    val destinationRotationAngle = rotationAngle.toFloat()
                    val deltaRotation = destinationRotationAngle - currentRotationAngle
                    currentRotationAngle = destinationRotationAngle

                    performRotation(deltaRotation)
                    baseView.updateImageRectangle()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
