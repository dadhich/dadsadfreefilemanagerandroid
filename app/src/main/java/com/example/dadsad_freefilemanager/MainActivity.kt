package com.example.dadsad_freefilemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {
    private val STORAGE_PERMISSION_CODE = 100
    private val MANAGE_STORAGE_CODE = 101
    private lateinit var fileListRecyclerView: RecyclerView
    private val fileList = mutableListOf<FileItem>()
    private val fullFileList = mutableListOf<FileItem>() // Store all files recursively
    private lateinit var fileAdapter: FileAdapter
    private var currentDir: File? = null // Will be set in onCreate
    private var selectedFileItem: FileItem? = null
    private var operationMode: String? = null // "copy" or "move"

    // Sort criteria and order
    private var sortBy: String = "name" // Default sort by name
    private var sortOrder: String = "asc" // Default ascending order

    // Search and filter variables
    private var currentQuery: String = ""
    private var filterType: String = "" // e.g., "txt", "pdf"
    private var filterSize: String = "All Sizes" // e.g., "< 1 MB"
    private var filterDate: String = "All Dates" // e.g., "Today"
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.app_name) // Set initial title to app name

        // Get the starting path from the intent
        val startPath = intent.getStringExtra("START_PATH") ?: Environment.getExternalStorageDirectory().absolutePath
        currentDir = File(startPath)

        fileListRecyclerView = findViewById(R.id.fileListRecyclerView)
        fileListRecyclerView.layoutManager = LinearLayoutManager(this)
        fileListRecyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        fileAdapter = FileAdapter(fileList) { fileItem ->
            if (operationMode != null) {
                if (fileItem.isDirectory) {
                    performFileOperation(fileItem.path)
                } else {
                    Toast.makeText(this, "Please select a folder as the destination", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (fileItem.isDirectory) {
                    currentDir = File(fileItem.path)
                    loadFiles()
                } else {
                    showFileDetails(fileItem)
                }
            }
        }
        fileAdapter.setCurrentDirPath(currentDir?.absolutePath ?: "")
        fileListRecyclerView.adapter = fileAdapter
        registerForContextMenu(fileListRecyclerView)

        checkAndRequestPermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_file_list, menu)

        // Set up the SearchView
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.queryHint = "Search files..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false // Not handling submit action
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText ?: ""
                debounceSearch()
                return true
            }
        })

        // Restore the full list when the SearchView is closed
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                currentQuery = ""
                filterFiles()
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> onSupportNavigateUp()
            R.id.action_sort -> {
                showSortMenu()
                true
            }
            R.id.action_filter -> {
                showFilterDialog()
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

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)
        val filterTypeEdit = dialogView.findViewById<EditText>(R.id.filter_type)
        val filterSizeSpinner = dialogView.findViewById<Spinner>(R.id.filter_size)
        val filterDateSpinner = dialogView.findViewById<Spinner>(R.id.filter_date)

        // Set up spinners
        ArrayAdapter.createFromResource(
            this,
            R.array.size_filter_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            filterSizeSpinner.adapter = adapter
            filterSizeSpinner.setSelection(
                resources.getStringArray(R.array.size_filter_options).indexOf(filterSize)
            )
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.date_filter_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            filterDateSpinner.adapter = adapter
            filterDateSpinner.setSelection(
                resources.getStringArray(R.array.date_filter_options).indexOf(filterDate)
            )
        }

        // Set current filter type
        filterTypeEdit.setText(filterType)

        AlertDialog.Builder(this)
            .setTitle("Filter Search Results")
            .setView(dialogView)
            .setPositiveButton("Apply") { _, _ ->
                filterType = filterTypeEdit.text.toString().trim().lowercase(Locale.getDefault())
                filterSize = filterSizeSpinner.selectedItem.toString()
                filterDate = filterDateSpinner.selectedItem.toString()
                filterFiles()
            }
            .setNegativeButton("Clear") { _, _ ->
                filterType = ""
                filterSize = "All Sizes"
                filterDate = "All Dates"
                filterTypeEdit.setText("")
                filterSizeSpinner.setSelection(0)
                filterDateSpinner.setSelection(0)
                filterFiles()
            }
            .setNeutralButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun debounceSearch() {
        // Remove any existing search task
        searchRunnable?.let { handler.removeCallbacks(it) }

        // Schedule a new search task with a 300ms delay
        searchRunnable = Runnable {
            filterFiles()
        }
        handler.postDelayed(searchRunnable!!, 300)
    }

    private fun filterFiles() {
        fileList.clear()
        fileAdapter.setSearchQuery(currentQuery) // For highlighting
        fileAdapter.setCurrentDirPath(currentDir?.absolutePath ?: "") // Update current directory path

        if (currentQuery.isEmpty() && filterType.isEmpty() && filterSize == "All Sizes" && filterDate == "All Dates") {
            // Show only the current directory's files if no search or filters are applied
            fileList.addAll(fullFileList.filter { File(it.path).parentFile?.absolutePath == currentDir?.absolutePath })
            sortAndUpdateFileList()
        } else {
            // Apply search and filters on the full recursive list
            var filteredList = fullFileList.toList()

            // Apply name search
            if (currentQuery.isNotEmpty()) {
                filteredList = filteredList.filter {
                    it.name.lowercase(Locale.getDefault()).contains(currentQuery.lowercase(Locale.getDefault()))
                }
            }

            // Apply file type filter
            if (filterType.isNotEmpty()) {
                filteredList = filteredList.filter {
                    val file = File(it.path)
                    !it.isDirectory && file.extension.lowercase(Locale.getDefault()) == filterType
                }
            }

            // Apply size filter
            if (filterSize != "All Sizes") {
                filteredList = filteredList.filter {
                    val file = File(it.path)
                    if (it.isDirectory) return@filter false // Exclude directories
                    val sizeInMB = file.length() / (1024 * 1024).toFloat() // Size in MB
                    when (filterSize) {
                        "< 1 MB" -> sizeInMB < 1
                        "1-10 MB" -> sizeInMB in 1.0..10.0
                        "> 10 MB" -> sizeInMB > 10
                        else -> true
                    }
                }
            }

            // Apply date filter
            if (filterDate != "All Dates") {
                filteredList = filteredList.filter {
                    val file = File(it.path)
                    val lastModified = file.lastModified()
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = lastModified

                    val now = Calendar.getInstance()
                    when (filterDate) {
                        "Today" -> {
                            calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                                    calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                        }
                        "This Week" -> {
                            now.add(Calendar.DAY_OF_YEAR, -7)
                            lastModified >= now.timeInMillis
                        }
                        "Older" -> {
                            now.add(Calendar.DAY_OF_YEAR, -7)
                            lastModified < now.timeInMillis
                        }
                        else -> true
                    }
                }
            }

            fileList.addAll(filteredList)
            sortAndUpdateFileList()

            if (filteredList.isEmpty()) {
                val message = if (currentQuery.isNotEmpty()) {
                    "No files found matching \"$currentQuery\""
                } else {
                    "No files found with the applied filters"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
        fileAdapter.notifyDataSetChanged()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (operationMode != null) {
            operationMode = null
            selectedFileItem = null
            Toast.makeText(this, "Operation cancelled", Toast.LENGTH_SHORT).show()
            loadFiles()
            return true
        } else {
            currentDir?.parentFile?.let { parent ->
                currentDir = parent
                loadFiles()
                return true
            } ?: run {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                return true
            }
        }
    }

    private fun loadFiles() {
        fileList.clear()
        fullFileList.clear()
        try {
            currentDir?.let { dir ->
                loadFilesRecursively(dir)
            }
            // Initially show only the current directory's files
            fileList.addAll(fullFileList.filter { File(it.path).parentFile?.absolutePath == currentDir?.absolutePath })
            if (fileList.isEmpty()) {
                Toast.makeText(this, "No files found in ${currentDir?.absolutePath}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error accessing files: ${e.message}", Toast.LENGTH_LONG).show()
        }
        fileAdapter.setCurrentDirPath(currentDir?.absolutePath ?: "")
        sortAndUpdateFileList()
        // Update toolbar title with current directory path
        supportActionBar?.title = currentDir?.absolutePath ?: "File Manager"
    }

    private fun loadFilesRecursively(dir: File) {
        try {
            val files = dir.listFiles()
            if (files != null) {
                files.forEach { file ->
                    val fileItem = FileItem(file.name, file.isDirectory, file.absolutePath)
                    fullFileList.add(fileItem)
                    if (file.isDirectory) {
                        loadFilesRecursively(file)
                    }
                }
            }
        } catch (e: Exception) {
            // Skip directories that can't be accessed
        }
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
            R.id.action_rename -> {
                selectedFileItem?.let { fileItem ->
                    showRenameDialog(fileItem)
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

    private fun showRenameDialog(fileItem: FileItem) {
        val editText = EditText(this).apply {
            setText(fileItem.name)
        }
        AlertDialog.Builder(this)
            .setTitle("Rename ${fileItem.name}")
            .setView(editText)
            .setPositiveButton("Rename") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty() && newName != fileItem.name) {
                    renameFile(fileItem, newName)
                } else if (newName.isEmpty()) {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun renameFile(fileItem: FileItem, newName: String) {
        try {
            val sourceFile = File(fileItem.path)
            val parentDir = sourceFile.parentFile
            val destFile = File(parentDir, newName)
            if (destFile.exists()) {
                Toast.makeText(this, "A file or folder with this name already exists", Toast.LENGTH_LONG).show()
                return
            }
            if (sourceFile.renameTo(destFile)) {
                Toast.makeText(this, "Renamed successfully", Toast.LENGTH_SHORT).show()
                loadFiles()
            } else {
                Toast.makeText(this, "Failed to rename ${fileItem.name}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Rename failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
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