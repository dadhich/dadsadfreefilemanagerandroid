package com.example.dadsad_freefilemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
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
    private var selectedFileItem: FileItem? = null
    private var operationMode: String? = null // "copy" or "move"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fileListRecyclerView = findViewById(R.id.fileListRecyclerView)
        backButton = findViewById(R.id.backButton)
        fileListRecyclerView.layoutManager = LinearLayoutManager(this)
        fileListRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        fileAdapter = FileAdapter(fileList) { fileItem ->
            if (operationMode != null) {
                // In copy/move mode, select destination folder
                if (fileItem.isDirectory) {
                    performFileOperation(fileItem.path)
                } else {
                    Toast.makeText(this, "Please select a folder as the destination", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Normal mode: navigate or show details
                if (fileItem.isDirectory) {
                    currentDir = File(fileItem.path)
                    loadFiles()
                } else {
                    showFileDetails(fileItem)
                }
            }
        }
        fileListRecyclerView.adapter = fileAdapter
        registerForContextMenu(fileListRecyclerView)

        backButton.setOnClickListener {
            if (operationMode != null) {
                // Cancel copy/move operation
                operationMode = null
                selectedFileItem = null
                Toast.makeText(this, "Operation cancelled", Toast.LENGTH_SHORT).show()
                loadFiles()
            } else {
                // Navigate to parent directory
                currentDir.parentFile?.let { parent ->
                    currentDir = parent
                    loadFiles()
                }
            }
        }

        checkAndRequestPermissions()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.menu_context, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        selectedFileItem = fileAdapter.getItemAtContextMenuPosition()
        return when (item.itemId) {
            R.id.action_copy -> {
                operationMode = "copy"
                Toast.makeText(this, "Select a destination folder to copy ${selectedFileItem?.name}", Toast.LENGTH_LONG).show()
                true
            }
            R.id.action_move -> {
                operationMode = "move"
                Toast.makeText(this, "Select a destination folder to move ${selectedFileItem?.name}", Toast.LENGTH_LONG).show()
                true
            }
            R.id.action_delete -> {
                selectedFileItem?.let { fileItem ->
                    AlertDialog.Builder(this)
                        .setTitle("Delete ${fileItem.name}")
                        .setMessage("Are you sure you want to delete this ${if (fileItem.isDirectory) "folder" else "file"}?")
                        .setPositiveButton("Yes") { _, _ ->
                            deleteFile(File(fileItem.path))
                            loadFiles()
                        }
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
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
        backButton.visibility = if (operationMode != null || currentDir.absolutePath == "/storage/emulated/0") View.GONE else View.VISIBLE
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

    private fun performFileOperation(destinationPath: String) {
        selectedFileItem?.let { fileItem ->
            val sourceFile = File(fileItem.path)
            val destFile = File(destinationPath, fileItem.name)
            try {
                when (operationMode) {
                    "copy" -> copyFileOrDirectory(sourceFile, destFile)
                    "move" -> {
                        copyFileOrDirectory(sourceFile, destFile)
                        deleteFile(sourceFile)
                    }
                }
                Toast.makeText(this, "${operationMode?.capitalize()} successful", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "${operationMode?.capitalize()} failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
            operationMode = null
            selectedFileItem = null
            loadFiles()
        }
    }

    private fun copyFileOrDirectory(source: File, dest: File) {
        if (source.isDirectory) {
            if (!dest.exists()) dest.mkdirs()
            source.listFiles()?.forEach { child ->
                copyFileOrDirectory(child, File(dest, child.name))
            }
        } else {
            FileInputStream(source).use { input ->
                FileOutputStream(dest).use { output ->
                    val channelIn: FileChannel? = input.channel
                    val channelOut: FileChannel? = output.channel
                    channelIn?.transferTo(0, channelIn.size(), channelOut)
                }
            }
        }
    }

    private fun deleteFile(file: File) {
        try {
            if (file.isDirectory) {
                file.listFiles()?.forEach { child -> deleteFile(child) }
            }
            if (file.delete()) {
                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete ${file.name}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}