package com.example.dadsad_freefilemanager

import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class FileListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private var fileList: MutableList<FileItem> = mutableListOf() // Changed to FileItem
    private var currentPath: String? = null

    // Sort criteria and order
    private var sortBy: String = "name" // Default sort by name
    private var sortOrder: String = "asc" // Default ascending order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get the path from the intent
        currentPath = intent.getStringExtra("PATH")
        if (currentPath == null) {
            currentPath = Environment.getExternalStorageDirectory().absolutePath
        }
        supportActionBar?.title = File(currentPath).name

        // Set up the RecyclerView
        recyclerView = findViewById(R.id.fileListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        fileAdapter = FileAdapter(fileList) { fileItem ->
            if (fileItem.isDirectory) {
                navigateToDirectory(fileItem.path)
            } else {
                Toast.makeText(this, "Selected file: ${fileItem.name}", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = fileAdapter

        // Load the initial file list
        loadFiles(currentPath!!)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_file_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_sort -> {
                showSortMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSortMenu() {
        val menuItemView = findViewById<View>(R.id.action_sort)
        val popupMenu = PopupMenu(this, menuItemView)
        popupMenu.menuInflater.inflate(R.menu.menu_sort_options, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sort_name_asc -> {
                    sortBy = "name"
                    sortOrder = "asc"
                }
                R.id.sort_name_desc -> {
                    sortBy = "name"
                    sortOrder = "desc"
                }
                R.id.sort_size_asc -> {
                    sortBy = "size"
                    sortOrder = "asc"
                }
                R.id.sort_size_desc -> {
                    sortBy = "size"
                    sortOrder = "desc"
                }
                R.id.sort_date_asc -> {
                    sortBy = "date"
                    sortOrder = "asc"
                }
                R.id.sort_date_desc -> {
                    sortBy = "date"
                    sortOrder = "desc"
                }
                R.id.sort_type_asc -> {
                    sortBy = "type"
                    sortOrder = "asc"
                }
                R.id.sort_type_desc -> {
                    sortBy = "type"
                    sortOrder = "desc"
                }
            }
            sortAndUpdateFileList()
            true
        }
        popupMenu.show()
    }

    private fun loadFiles(path: String) {
        val directory = File(path)
        if (!directory.exists() || !directory.isDirectory) {
            Toast.makeText(this, "Invalid directory", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val files = directory.listFiles()?.toList() ?: emptyList()
        fileList.clear()
        fileList.addAll(files.map { file ->
            FileItem(file.name, file.isDirectory, file.absolutePath)
        })
        sortAndUpdateFileList()
    }

    private fun sortAndUpdateFileList() {
        // Separate directories and files for better organization
        val directories = fileList.filter { it.isDirectory }
        val files = fileList.filter { !it.isDirectory }

        // Sort directories
        val sortedDirectories = directories.sortedWith(getComparator())
        // Sort files
        val sortedFiles = files.sortedWith(getComparator())

        // Combine the lists: directories first, then files
        fileList.clear()
        fileList.addAll(sortedDirectories)
        fileList.addAll(sortedFiles)

        // Notify the adapter to refresh the RecyclerView
        fileAdapter.notifyDataSetChanged()
    }

    private fun getComparator(): Comparator<FileItem> {
        return when (sortBy) {
            "name" -> Comparator { item1, item2 ->
                val comparison = item1.name.compareTo(item2.name, ignoreCase = true)
                if (sortOrder == "asc") comparison else -comparison
            }
            "size" -> Comparator { item1, item2 ->
                val file1 = File(item1.path)
                val file2 = File(item2.path)
                val comparison = file1.length().compareTo(file2.length())
                if (sortOrder == "asc") comparison else -comparison
            }
            "date" -> Comparator { item1, item2 ->
                val file1 = File(item1.path)
                val file2 = File(item2.path)
                val comparison = file1.lastModified().compareTo(file2.lastModified())
                if (sortOrder == "asc") comparison else -comparison
            }
            "type" -> Comparator { item1, item2 ->
                val file1 = File(item1.path)
                val file2 = File(item2.path)
                val ext1 = file1.extension.lowercase(Locale.getDefault())
                val ext2 = file2.extension.lowercase(Locale.getDefault())
                val comparison = ext1.compareTo(ext2)
                if (sortOrder == "asc") comparison else -comparison
            }
            else -> Comparator { item1, item2 ->
                item1.name.compareTo(item2.name, ignoreCase = true)
            }
        }
    }

    private fun navigateToDirectory(path: String) {
        val intent = intent
        intent.putExtra("PATH", path)
        startActivity(intent)
        finish()
    }
}