package com.example.dadsad_freefilemanager

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import java.io.File
import kotlin.concurrent.thread

class AppsActivity : AppCompatActivity() {
    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var appsAdapter: AppsAdapter
    private val allApps = mutableListOf<AppItem>()
    private val downloadedApps = mutableListOf<AppItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Apps"

        appsRecyclerView = findViewById(R.id.appsRecyclerView)
        appsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load apps in a background thread
        thread {
            loadApps()
            runOnUiThread {
                appsAdapter = AppsAdapter(downloadedApps)
                appsRecyclerView.adapter = appsAdapter

                // Set up TabLayout
                val tabLayout: TabLayout = findViewById(R.id.appsTabLayout)
                tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        when (tab?.position) {
                            0 -> {
                                appsAdapter = AppsAdapter(downloadedApps)
                                appsRecyclerView.adapter = appsAdapter
                            }
                            1 -> {
                                appsAdapter = AppsAdapter(allApps)
                                appsRecyclerView.adapter = appsAdapter
                            }
                        }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                })
            }
        }
    }

    private fun loadApps() {
        val pm: PackageManager = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (packageInfo in packages) {
            val appName = packageInfo.loadLabel(pm).toString()
            val packageName = packageInfo.packageName
            val icon = packageInfo.loadIcon(pm)

            // Get app size
            val apkPath = packageInfo.sourceDir
            val apkFile = File(apkPath)
            val size = apkFile.length()

            val appItem = AppItem(appName, packageName, size, icon)
            allApps.add(appItem)

            // Check if the app is a user-installed (downloaded) app
            if ((packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                downloadedApps.add(appItem)
            }
        }

        // Sort apps by name
        allApps.sortBy { it.name }
        downloadedApps.sortBy { it.name }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        return true
    }
}