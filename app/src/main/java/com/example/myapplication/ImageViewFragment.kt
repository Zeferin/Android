package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image_view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ImageViewFragment : Fragment {
    var imageUri: Uri?

    // base constructor will also call inflate
    constructor(imageUri: Uri?) : super(R.layout.fragment_image_view) {
        Log.d("fragment",imageUri.toString())
        this.imageUri = imageUri
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // spawn a new coroutine to load the image
        lifecycleScope.launch {
            Picasso.get().load(imageUri).fit().into(imageViewC)
        }
    }
}