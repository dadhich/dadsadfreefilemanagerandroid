package com.example.dadsad_freefilemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val STORAGE_PERMISSION_CODE = 100
    private val MANAGE_STORAGE_CODE = 101
    private lateinit var fileListRecyclerView: RecyclerView
    private val fileList = mutableListOf<FileItem>()
    private lateinit var fileAdapter: FileAdapter
    private lateinit var backButton: Button
    private var currentDir: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        File("/storage/emulated/0/")
    } else {
        Environment.getExternalStorageDirectory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fileListRecyclerView = findViewById(R.id.fileListRecyclerView)
        backButton = findViewById(R.id.backButton)
        fileListRecyclerView.layoutManager = LinearLayoutManager(this)
        fileAdapter = FileAdapter(fileList) { fileItem ->
            if (fileItem.isDirectory) {
                // Navigate into the folder
                currentDir = File(fileItem.path)
                loadFiles()
            } else {
                // Show file details
                showFileDetails(fileItem)
            }
        }
        fileListRecyclerView.adapter = fileAdapter

        backButton.setOnClickListener {
            // Navigate to parent directory
            currentDir.parentFile?.let { parent ->
                currentDir = parent
                loadFiles()
            }
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Please grant all files access permission", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, MANAGE_STORAGE_CODE)
            } else {
                loadFiles()
            }
        } else {
            if (checkStoragePermissions()) {
                loadFiles()
            } else {
                requestStoragePermissions()
            }
        }
    }

    private fun checkStoragePermissions(): Boolean {
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return readPermission == PackageManager.PERMISSION_GRANTED &&
                writePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Storage permissions are required to list files", Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permissions granted", Toast.LENGTH_SHORT).show()
                loadFiles()
            } else {
                Toast.makeText(this, "Permissions denied. Please grant permissions to proceed.", Toast.LENGTH_LONG).show()
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } else {
                    checkAndRequestPermissions()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MANAGE_STORAGE_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Manage storage permission granted", Toast.LENGTH_SHORT).show()
                loadFiles()
            } else {
                Toast.makeText(this, "Permissions denied. Please grant all files access to proceed.", Toast.LENGTH_LONG).show()
                checkAndRequestPermissions()
            }
        }
    }

    private fun loadFiles() {
        fileList.clear()
        try {
            val files = currentDir.listFiles()
            if (files != null) {
                files.forEach { file ->
                    fileList.add(FileItem(file.name, file.isDirectory, file.absolutePath))
                }
                if (fileList.isEmpty()) {
                    Toast.makeText(this, "No files found in ${currentDir.absolutePath}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Unable to access files in ${currentDir.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error accessing files: ${e.message}", Toast.LENGTH_LONG).show()
        }
        fileAdapter.notifyDataSetChanged()
        backButton.visibility = if (currentDir.absolutePath == "/storage/emulated/0") View.GONE else View.VISIBLE
    }

    private fun showFileDetails(fileItem: FileItem) {
        val file = File(fileItem.path)
        val size = file.length() / 1024 // Size in KB
        val lastModified = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(file.lastModified()))
        val details = """
            Name: ${fileItem.name}
            Path: ${fileItem.path}
            Size: $size KB
            Last Modified: $lastModified
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("File Details")
            .setMessage(details)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}