package com.example.myapplication

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Throws(IOException::class)
fun createImageFile(storageDir: File?): File {
    // Create an image file name
    val timeStamp: String = generateImageSuffix()
    Log.d("util",storageDir?.absolutePath)
    return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    ).apply {
        // Save a file: path for use with ACTION_VIEW intents
        //currentPhotoPath = absolutePath
    }
}

public fun generateImageSuffix() = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())