package com.example.imageeditingapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.imageeditingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var galleryManager: GalleryManager
    private lateinit var rotationHelper: RotationHelper

    /*
        OnCreate:
        1. Initialize binding
        2. Display Image on Screen with crop and rotate features
        3. Control rotation of image
        4. Control crop feature with crop button
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        manageGallery()
        controlImageRotation()
        controlImageCrop()
    }

    private fun controlImageCrop() {
        binding.buttonCropImage.setOnClickListener {
            binding.imageToEdit.switchCropMode()
        }
    }

    /*
    controlImageRotation: Toggle the slider on click
     */
    private fun controlImageRotation() {
        rotationHelper = RotationHelper(binding.imageToEdit, binding.seekbarRotateImage)

        binding.buttonRotateImage.setOnClickListener {
            binding.seekbarRotateImage.visibility =
                if (binding.seekbarRotateImage.visibility == View.VISIBLE)
                    View.GONE
                else
                    View.VISIBLE
        }
    }

    /*
    manageGallery:
        1. Store the instance of class Gallery Manager for this session
        2. On click of select image button, request permission to open the gallery
        3. Display/Deny depending on the action taken by Gallery Manager
        4. Normalize (fit the image within the imageview is displayed)
     */
    private fun manageGallery() {

        fun displayImage(uri: Uri) {
            binding.imageToEdit.setImageURI(uri)
            binding.appFeatures.visibility = View.VISIBLE
            binding.seekbarRotateImage.progress = 180
            rotationHelper.performRotation(0f)

            binding.imageToEdit.post {
                binding.imageToEdit.initializeImage()
            }
        }

        galleryManager = GalleryManager(this, { uri -> displayImage(uri) } )

        binding.buttonSelectImage.setOnClickListener {
            galleryManager.requestPermission()
        }
    }

}