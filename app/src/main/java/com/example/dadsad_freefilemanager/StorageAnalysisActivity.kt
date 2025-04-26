package com.example.dadsad_freefilemanager

import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.io.File
import java.text.DecimalFormat
import kotlin.concurrent.thread

class StorageAnalysisActivity : AppCompatActivity() {
    private lateinit var totalStorageText: TextView
    private lateinit var storageProgressBar: ProgressBar
    private lateinit var storagePercentageText: TextView
    private lateinit var storagePieChart: PieChart
    private lateinit var largeFilesHeader: TextView
    private lateinit var largeFilesSubheader: TextView
    private lateinit var largeFilesContainer: LinearLayout
    private lateinit var recycleBinHeader: TextView
    private lateinit var recycleBinContainer: LinearLayout

    private var imagesSize: Long = 0
    private var audioSize: Long = 0
    private var videosSize: Long = 0
    private var documentsSize: Long = 0
    private var archivesSize: Long = 0
    private var othersSize: Long = 0
    private var largeFiles: MutableList<File> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_analysis)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Storage Analysis"

        // Initialize UI elements
        totalStorageText = findViewById(R.id.totalStorageText)
        storageProgressBar = findViewById(R.id.storageProgressBar)
        storagePercentageText = findViewById(R.id.storagePercentageText)
        storagePieChart = findViewById(R.id.storagePieChart)
        largeFilesHeader = findViewById(R.id.largeFilesHeader)
        largeFilesSubheader = findViewById(R.id.largeFilesSubheader)
        largeFilesContainer = findViewById(R.id.largeFilesContainer)
        recycleBinHeader = findViewById(R.id.recycleBinHeader)
        recycleBinContainer = findViewById(R.id.recycleBinContainer)

        // Set up the pie chart
        setupPieChart()

        // Calculate storage usage in a background thread
        thread {
            calculateStorageUsage()
            runOnUiThread {
                updateUI()
            }
        }
    }

    private fun setupPieChart() {
        storagePieChart.description.isEnabled = false
        storagePieChart.setUsePercentValues(true)
        storagePieChart.setDrawHoleEnabled(true)
        storagePieChart.setHoleColor(android.graphics.Color.WHITE)
        storagePieChart.setTransparentCircleColor(android.graphics.Color.WHITE)
        storagePieChart.setTransparentCircleAlpha(110)
        storagePieChart.holeRadius = 58f
        storagePieChart.transparentCircleRadius = 61f
        storagePieChart.setDrawCenterText(true)
        storagePieChart.centerText = "Storage Breakdown"
        storagePieChart.setCenterTextSize(14f)
        storagePieChart.legend.isEnabled = true
        storagePieChart.setEntryLabelColor(android.graphics.Color.BLACK)
        storagePieChart.setEntryLabelTextSize(12f)
        storagePieChart.animateY(1400)
    }

    private fun calculateStorageUsage() {
        // Calculate total and free storage
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val totalBytes = stat.blockSizeLong * stat.blockCountLong
        val freeBytes = stat.blockSizeLong * stat.availableBlocksLong

        // Update total storage and percentage
        val usedBytes = totalBytes - freeBytes
        val percentageUsed = (usedBytes.toDouble() / totalBytes * 100).toInt()
        runOnUiThread {
            totalStorageText.text = "Main storage: ${formatSize(freeBytes)} free"
            storageProgressBar.progress = percentageUsed
            storagePercentageText.text = "$percentageUsed%"
        }

        // Analyze files by type
        val rootDir = Environment.getExternalStorageDirectory()
        analyzeDirectory(rootDir)

        // Sort large files by size (descending)
        largeFiles.sortByDescending { it.length() }
    }

    private fun analyzeDirectory(dir: File) {
        try {
            val files = dir.listFiles() ?: return
            for (file in files) {
                if (file.isDirectory) {
                    analyzeDirectory(file)
                } else {
                    val size = file.length()
                    // Check for large files (>10 MB)
                    if (size > 10 * 1024 * 1024) {
                        largeFiles.add(file)
                    }
                    // Categorize by file type
                    when (file.extension.lowercase()) {
                        "jpg", "jpeg", "png", "gif", "bmp" -> imagesSize += size
                        "mp3", "wav", "ogg", "m4a" -> audioSize += size
                        "mp4", "mkv", "avi", "mov" -> videosSize += size
                        "pdf", "doc", "docx", "txt" -> documentsSize += size
                        "zip", "rar", "7z" -> archivesSize += size
                        else -> othersSize += size
                    }
                }
            }
        } catch (e: Exception) {
            // Skip directories that can't be accessed
        }
    }

    private fun updateUI() {
        // Update pie chart
        val entries = mutableListOf<PieEntry>()
        val totalUsed = imagesSize + audioSize + videosSize + documentsSize + archivesSize + othersSize

        if (totalUsed > 0) {
            if (imagesSize > 0) entries.add(PieEntry(imagesSize.toFloat(), "Images (${formatSize(imagesSize)})"))
            if (audioSize > 0) entries.add(PieEntry(audioSize.toFloat(), "Audio (${formatSize(audioSize)})"))
            if (videosSize > 0) entries.add(PieEntry(videosSize.toFloat(), "Videos (${formatSize(videosSize)})"))
            if (documentsSize > 0) entries.add(PieEntry(documentsSize.toFloat(), "Documents (${formatSize(documentsSize)})"))
            if (archivesSize > 0) entries.add(PieEntry(archivesSize.toFloat(), "Archives (${formatSize(archivesSize)})"))
            if (othersSize > 0) entries.add(PieEntry(othersSize.toFloat(), "Others (${formatSize(othersSize)})"))

            val dataSet = PieDataSet(entries, "")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = android.graphics.Color.BLACK
            dataSet.valueFormatter = PercentFormatter(storagePieChart)

            val pieData = PieData(dataSet)
            storagePieChart.data = pieData
            storagePieChart.invalidate()
        } else {
            storagePieChart.data = null
            storagePieChart.centerText = "No data to display"
            storagePieChart.invalidate()
        }

        // Update large files section
        val totalLargeFilesSize = largeFiles.sumOf { it.length() }
        largeFilesHeader.text = "Large Files (${formatSize(totalLargeFilesSize)})"
        if (largeFiles.isEmpty()) {
            largeFilesSubheader.text = "No files larger than 10 MB"
        } else {
            largeFilesSubheader.text = "Files larger than 10 MB"
            largeFiles.take(2).forEach { file ->
                val view = LayoutInflater.from(this).inflate(R.layout.item_large_file, largeFilesContainer, false)
                val fileNameText: TextView = view.findViewById(R.id.fileNameText)
                val fileSizeText: TextView = view.findViewById(R.id.fileSizeText)
                fileNameText.text = file.name
                fileSizeText.text = formatSize(file.length())
                largeFilesContainer.addView(view)
            }
            if (largeFiles.size > 2) {
                val moreText = TextView(this).apply {
                    text = "MORE"
                    textSize = 14f
                    setTextColor(resources.getColor(android.R.color.holo_blue_dark))
                    setPadding(0, 8, 0, 0)
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                }
                largeFilesContainer.addView(moreText)
            }
        }

        // Update Recycle Bin section (placeholder)
        recycleBinHeader.text = "Recycle Bin (0.00 B)"
        val placeholderText = TextView(this).apply {
            text = "Recycle Bin not implemented yet"
            textSize = 14f
            setPadding(8, 8, 8, 8)
        }
        recycleBinContainer.addView(placeholderText)
    }

    private fun formatSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return "${DecimalFormat("#,##0.##").format(size)} ${units[unitIndex]}"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        return true
    }
}