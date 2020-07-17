package com.example.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.Manifest
import android.app.Activity
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_CODE = 1000
        const val IMAGE_PICK_CODE = 1001
    }

    lateinit var imageResult: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonClear: Button = findViewById(R.id.bt_clear)
        val buttonSubmit: Button = findViewById(R.id.bt_submit)
        imageResult = findViewById(R.id.iv_result)

        buttonClear.setOnClickListener {
            imageResult.setImageBitmap(null)
        }

        buttonSubmit.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_CODE
                )
            } else {
                pickImageFromGallery()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted
                    pickImageFromGallery()
                }
            }
        }
    }

    private fun pickImageFromGallery() {
        val impagePickIntent = Intent(Intent.ACTION_PICK).setType("image/*")
        startActivityForResult(impagePickIntent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE)
            Picasso.get().load(data?.data).fit().into(imageResult)

    }


}