package com.example.dadsad_freefilemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dadsad_freefilemanager.R

class FileAdapter(
    private val fileList: List<FileItem>,
    private val onItemClick: (FileItem) -> Unit // Callback for item clicks
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)
        val fileTypeTextView: TextView = itemView.findViewById(R.id.fileTypeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileItem = fileList[position]
        holder.fileNameTextView.text = fileItem.name
        holder.fileTypeTextView.text = if (fileItem.isDirectory) "Folder" else "File"
        holder.itemView.setOnClickListener {
            onItemClick(fileItem) // Trigger the callback when an item is clicked
        }
    }

    override fun getItemCount(): Int = fileList.size
}