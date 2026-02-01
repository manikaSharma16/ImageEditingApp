package com.example.imageeditingapp.utils

import android.graphics.drawable.Drawable

object DrawableUtils {

    // Check if the imaeg exists and the view containing image has a layed out structure
    fun hasImageAndLayout(image: Drawable, viewWidth: Int, viewHeight: Int): Boolean {
        if(image==null)
            return false

        if (viewWidth == 0 || viewHeight == 0)
            return false

        return true
    }

    // Get the native resolution of the image : local coordinates
    fun getImageSize(image: Drawable): Pair<Float, Float>? {
        if(image==null)
            return null

        val imageWidth = image.intrinsicWidth.toFloat()
        val imageHeight = image.intrinsicHeight.toFloat()
        return imageWidth to imageHeight
    }
}