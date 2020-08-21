package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_CODE_IMAGE = 1000
        const val PERMISSION_CODE_VIDEO = 1001
        const val IMAGE_PICK_CODE = 1002
        const val VIDEO_PICK_CODE = 1003
        const val REQUEST_IMAGE_CAPTURE = 1004
        const val REQUEST_VIDEO_CAPTURE = 1005
        const val PERMISSION_CAPTURE_IMAGE = 1006
    }

    private var currentPhotoUri: Uri? = null
    private var currentPhotoPath: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch{
            Log.d("Version", ""+ Build.VERSION.SDK_INT)
            initOpenVideoButton(findViewById(R.id.openVideo))
            initOpenPhotoButton(findViewById(R.id.openPhoto))
            initCaptureVideoButton(findViewById(R.id.captureVideo))

            // potentially blocking operation (create file&directory)
            // the button click will have no effect until this coroutine finishes its job. Then the user will have to click again
            initCapturePhotoButton(findViewById(R.id.capturePhoto))
        }
    }

    private fun initCapturePhotoButton(takePhoto: Button) {
        takePhoto.setOnClickListener {
            if (checkAndRequestPermissions(
                    PERMISSION_CAPTURE_IMAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                capturePhoto()
            }
        }
    }

    private fun capturePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                if (Build.VERSION.SDK_INT < 29)
                    createFileForImageExport(takePictureIntent)
                else
                {
                    val resolver: ContentResolver = getContentResolver()
                    val contentValues = ContentValues()
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,  generateImageSuffix())
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE,     "image/png")
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
                    currentPhotoUri =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    Log.d("resultedUri","imageUri")
                    //   fos = resolver.openOutputStream(imageUri!!)
                    Log.d("fileCreationUri", currentPhotoUri.toString())
                    currentPhotoPath = currentPhotoUri?.path;
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun createFileForImageExport(takePictureIntent: Intent) {
        // Create the File where the photo should go
        val photoFile: File? = try {
            createImageFile(getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM))
        } catch (ex: IOException) {
            // Error occurred while creating the File
            Log.d("fileCreation", ex.message)
            null
        }
        if (photoFile != null) {
            currentPhotoPath = photoFile.absolutePath
            Log.d("fileCreation", currentPhotoPath)
        }
        // Continue only if the File was successfully created
        photoFile?.also {
            currentPhotoUri  = FileProvider.getUriForFile(
                this,
                "com.example.myapplication.fileprovider",
                it
            )
            Log.d("fileCreationUri", currentPhotoUri.toString())
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun initCaptureVideoButton(recordVideo: Button) {

    }

    private fun initOpenPhotoButton(openPhoto: Button) {
        openPhoto.setOnClickListener {
            if (checkAndRequestPermissions(
                    PERMISSION_CODE_IMAGE, Manifest.permission.READ_EXTERNAL_STORAGE)){
                pickImageFromGallery()
            }
        }
    }

    private fun initOpenVideoButton(openVideo: Button) {
        openVideo.setOnClickListener {
            if (checkAndRequestPermissions(
                    PERMISSION_CODE_VIDEO, Manifest.permission.READ_EXTERNAL_STORAGE)){
                pickVideoFromGallery()
            }
        }
    }

    private fun checkAndRequestPermissions(code: Int, permission: String):Boolean {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
            requestPermissions( arrayOf(permission), code)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray)
    {
        if ((grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED))
        {
            when (requestCode) {
                PERMISSION_CODE_IMAGE ->  pickImageFromGallery()
                PERMISSION_CODE_VIDEO -> pickVideoFromGallery()
                PERMISSION_CAPTURE_IMAGE -> capturePhoto()
            }
        }
    }

    private fun pickImageFromGallery() {
        val impagePickIntent = Intent(Intent.ACTION_PICK).setType("image/*")
        startActivityForResult(impagePickIntent, IMAGE_PICK_CODE)
    }

    private fun pickVideoFromGallery() {
        val impagePickIntent = Intent(Intent.ACTION_PICK).setType("video/*")
        startActivityForResult(impagePickIntent, VIDEO_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
        {
            when (requestCode) {
                IMAGE_PICK_CODE -> displayFragment(ImageViewFragment(data?.data!!))
                VIDEO_PICK_CODE -> displayFragment(VideoViewFragment(data?.data!!))
                REQUEST_IMAGE_CAPTURE -> {
                    if (Build.VERSION.SDK_INT < 29) {
                        var f = File(currentPhotoPath)
                        currentPhotoUri = Uri.fromFile(f)
                    }
                    displayImageFromUri()
                }

            }
        }
        else
        {
            Log.d("error","result not ok")
        }
    }

    private fun displayImageFromUri() {
        displayFragment(ImageViewFragment(currentPhotoUri))
        Log.d("capture", currentPhotoUri.toString())
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            mediaScanIntent.data = currentPhotoUri
            sendBroadcast(mediaScanIntent)
        }
    }

    private fun displayFragment(fragment : Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment, fragment)
            addToBackStack(null)
            commit()
        }
    }
}