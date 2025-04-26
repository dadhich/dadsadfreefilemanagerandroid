package com.example.dadsad_freefilemanager

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class LibraryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        // Set up click listeners for each media type
        view.findViewById<View>(R.id.imagesButton).setOnClickListener {
            startMainActivityWithFilter("images", listOf("jpg", "jpeg", "png", "gif", "bmp"))
        }

        view.findViewById<View>(R.id.audioButton).setOnClickListener {
            startMainActivityWithFilter("audio", listOf("mp3", "wav", "ogg", "m4a"))
        }

        view.findViewById<View>(R.id.videosButton).setOnClickListener {
            startMainActivityWithFilter("videos", listOf("mp4", "mkv", "avi", "mov"))
        }

        view.findViewById<View>(R.id.documentsButton).setOnClickListener {
            startMainActivityWithFilter("documents", listOf("pdf", "doc", "docx", "txt"))
        }

        view.findViewById<View>(R.id.newFilesButton).setOnClickListener {
            startMainActivityWithDateFilter("new_files", "This Week")
        }

        return view
    }

    private fun startMainActivityWithFilter(type: String, extensions: List<String>) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("START_PATH", Environment.getExternalStorageDirectory().absolutePath)
            putExtra("FILTER_TYPE", extensions.joinToString(","))
            putExtra("AUTO_SEARCH", true)
        }
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun startMainActivityWithDateFilter(type: String, dateFilter: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("START_PATH", Environment.getExternalStorageDirectory().absolutePath)
            putExtra("FILTER_DATE", dateFilter)
            putExtra("AUTO_SEARCH", true)
        }
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}