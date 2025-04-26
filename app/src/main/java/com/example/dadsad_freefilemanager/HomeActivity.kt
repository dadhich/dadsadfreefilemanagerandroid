package com.example.dadsad_freefilemanager

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.io.File

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Update storage info
        updateStorageInfo()

        // Set up button click listeners for navigation
        findViewById<LinearLayout>(R.id.mainStorageButton).setOnClickListener {
            navigateToFileList(Environment.getExternalStorageDirectory().absolutePath)
        }

        findViewById<LinearLayout>(R.id.sdCardButton).setOnClickListener {
            // Note: SD card path varies by device; this is a placeholder
            val sdCardPath = "/storage/extSdCard" // Adjust based on actual SD card path
            navigateToFileList(sdCardPath)
        }

        findViewById<LinearLayout>(R.id.downloadsButton).setOnClickListener {
            navigateToFileList(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath)
        }

        findViewById<LinearLayout>(R.id.appsButton).setOnClickListener {
            Toast.makeText(this, "Apps section not implemented yet", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.recycleBinButton).setOnClickListener {
            Toast.makeText(this, "Recycle Bin not implemented", Toast.LENGTH_SHORT).show()
        }

        val analyzeStorageButton: Button = findViewById(R.id.analyzeStorageButton)
        analyzeStorageButton.setOnClickListener {
            val intent = Intent(this, StorageAnalysisActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Tab click listeners (for future implementation)
        findViewById<TextView>(R.id.localTab).setOnClickListener {
            Toast.makeText(this, "Local tab selected", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.libraryTab).setOnClickListener {
            Toast.makeText(this, "Library tab not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStorageInfo() {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        val blockSize: Long
        val totalBlocks: Long
        val availableBlocks: Long

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.blockSizeLong
            totalBlocks = stat.blockCountLong
            availableBlocks = stat.availableBlocksLong
        } else {
            blockSize = stat.blockSize.toLong()
            totalBlocks = stat.blockCount.toLong()
            availableBlocks = stat.availableBlocks.toLong()
        }

        val totalSize = (totalBlocks * blockSize) / (1024 * 1024 * 1024) // GB
        val freeSize = (availableBlocks * blockSize) / (1024 * 1024 * 1024) // GB
        val usedSize = totalSize - freeSize
        val usedPercentage = if (totalSize > 0) (usedSize * 100 / totalSize).toInt() else 0

        findViewById<TextView>(R.id.storageUsageText).text = "$usedPercentage%"
        findViewById<ProgressBar>(R.id.storageProgressBar).progress = usedPercentage
        findViewById<TextView>(R.id.storageDetailsText).text = "Main storage\n$usedSize GB / $totalSize GB"

        // Placeholder for media sizes (requires scanning the storage, which we'll implement later if needed)
    }

    private fun navigateToFileList(path: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("START_PATH", path)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}