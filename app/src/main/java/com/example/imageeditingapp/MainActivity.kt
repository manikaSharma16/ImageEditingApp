package com.example.imageeditingapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.imageeditingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var galleryManager: GalleryManager

    /*
        OnCreate:
        1. Initialize binding
        2. Display Image on Screen with crop and rotate features
        3. Control rotation of image
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        manageGallery()
        controlImageRotation()
    }

    /*
    controlImageRotation: Toggle the slider on click
     */
    private fun controlImageRotation() {
        binding.buttonRotateImage.setOnClickListener {
            binding.seekbarRotateImage.visibility =
                if (binding.seekbarRotateImage.visibility == View.VISIBLE)
                    View.GONE
                else
                    View.VISIBLE
        }

        binding.seekbarRotateImage.setOnSeekBarChangeListener(
            binding.imageToEdit.RotationSliderListener()
        )
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

            binding.imageToEdit.post {
                binding.imageToEdit.normalize()
            }
        }

        galleryManager = GalleryManager(this, { uri -> displayImage(uri) } )

        binding.buttonSelectImage.setOnClickListener {
            galleryManager.requestPermission()
        }
    }

}