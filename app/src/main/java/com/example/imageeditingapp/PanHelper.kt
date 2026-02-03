package com.example.imageeditingapp

import android.view.MotionEvent

class PanHelper(private val baseView: AffineImageTransformView) {
    private var prevTouchX = 0f
    private var prevTouchY = 0f

    // Translate by the drag
    fun performPan(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> { // On-Finger-Touch
                prevTouchX = event.x
                prevTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> { // On-Finger-Move
                val curTouchX = event.x
                val curTocuhY = event.y
                val dx = curTouchX - prevTouchX
                val dy = curTocuhY - prevTouchY
                baseView.imageMatrix.postTranslate(dx, dy)
                prevTouchX = event.x
                prevTouchY = event.y
                baseView.invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { // On-Finger-Interaction-End
            }
        }

        return true
    }
}