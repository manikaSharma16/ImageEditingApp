package com.example.imageeditingapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class GalleryManager(
    private val activity: AppCompatActivity,
    private val onImageSelected: (Uri) -> Unit) {

    /*
    Depending on the android version, Select the gallery permission
     */
    private val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
    else
            Manifest.permission.READ_EXTERNAL_STORAGE

    /*
    Handles the system permission request result.
        Granted -> opens the gallery
        Denied -> ppermision denied toast
     */
    private val requestPermissionLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                openGallery()
            } else {
                Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    /*
    Handles the gallery picker request result
        Result: Selected image result
        call onImageSelected(uri) on succesful image selection
     */
    private val imageLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    onImageSelected(uri)
                }
            }
        }

    /* requestPermission:
        Dispaly AlertDialog
            Tap Allow -> proceeds to permission check
            Taps Deny -> permission denied toast
     */
    fun requestPermission() {

        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.
            setTitle("Gallery Permission")
            .setMessage("ImageEditingApp needs access to your gallery to select images")
            .setPositiveButton("Allow") {  _, _ -> requestPermissionHelper()
            }
            .setNegativeButton("Deny") { _, _ ->
                Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /*
    requestPermissionHelper:
        Check if gallery permission is already granted
            -> Granted -> opens gallery
            -> Not granted, launch system permission request
     */
    private fun requestPermissionHelper() {
        val granted = ContextCompat.checkSelfPermission(activity, permission) ==
                PackageManager.PERMISSION_GRANTED
        if(granted)
            openGallery()
        else{
            requestPermissionLauncher.launch(permission)
        }
    }

    /* Open Gallery */
    private fun openGallery() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        imageLauncher.launch(intent)
    }
}