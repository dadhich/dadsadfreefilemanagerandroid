package com.example.dadsad_freefilemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class FileAdapter(
    private val files: MutableList<FileItem>,
    private val onClick: (FileItem) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private var contextMenuPosition: Int = -1 // Track the position for context menu

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.fileIcon)
        val name: TextView = itemView.findViewById(R.id.fileName)
        val details: TextView = itemView.findViewById(R.id.fileDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view).apply {
            // Set up long-click listener to open context menu
            itemView.setOnLongClickListener {
                contextMenuPosition = adapterPosition
                false // Let the system handle the context menu
            }
            // Set up click listener
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClick(files[position])
                }
            }
        }
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileItem = files[position]
        holder.name.text = fileItem.name
        holder.icon.setImageResource(
            if (fileItem.isDirectory) R.drawable.ic_folder else R.drawable.ic_file
        )

        val file = File(fileItem.path)
        val details = if (fileItem.isDirectory) {
            "Folder"
        } else {
            val size = file.length() / 1024 // Size in KB
            val lastModified = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                .format(file.lastModified())
            "Size: $size KB | Modified: $lastModified"
        }
        holder.details.text = details
    }

    override fun getItemCount(): Int = files.size

    // Add method to get the item at the context menu position
    fun getItemAtContextMenuPosition(): FileItem? {
        return if (contextMenuPosition != -1 && contextMenuPosition < files.size) {
            files[contextMenuPosition]
        } else {
            null
        }
    }
}