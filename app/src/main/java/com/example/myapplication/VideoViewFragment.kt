package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.MediaController
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_video_view.*
import kotlinx.coroutines.launch


class VideoViewFragment : Fragment {
    var videoUri: Uri

    // base constructor will also call inflate
    constructor(videoUri: Uri) : super(R.layout.fragment_video_view) {
        this.videoUri = videoUri
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mediaController = MediaController(activity)
        mediaController.setAnchorView(videoView)

        // spawn a new coroutine to load the video
        lifecycleScope.launch {
            videoView.setMediaController(mediaController)
            videoView.setVideoURI(videoUri)
            videoView.requestFocus()
            videoView.start()
        }
    }
}