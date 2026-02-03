package com.example.imageeditingapp

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

        baseView.imageMatrix.mapRect(baseView.imageRectangle)
        baseView.invalidate()
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
