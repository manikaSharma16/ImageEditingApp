package com.example.imageeditingapp

val appName = "ImageEditingApp"

// Gallery Manager
val allow = "Allow"
val deny = "Deny"
val permissionDenied = "Permission Denied"

// Scaling
val scaleWithinBounds = true
val minScaleFactor = 0.5f

// Crop
val cropRectanglePadding = 0.01f
val cropRectangleWidth = 4f

enum class ACTIVECROPEDGE {
    LEFT, RIGHT, TOP, BOTTOM,
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
    NONE
}

val touchTolerance = 50f
val minCropRectangleHeightWidth = 100f