package com.example.dadsad_freefilemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

class AppsAdapter(private val apps: List<AppItem>) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        val appNameText: TextView = itemView.findViewById(R.id.appNameText)
        val appSizeText: TextView = itemView.findViewById(R.id.appSizeText)
        val appCheckBox: CheckBox = itemView.findViewById(R.id.appCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.appIcon.setImageDrawable(app.icon)
        holder.appNameText.text = app.name
        holder.appSizeText.text = formatSize(app.size)
        holder.appCheckBox.isChecked = false
    }

    override fun getItemCount(): Int = apps.size

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
}