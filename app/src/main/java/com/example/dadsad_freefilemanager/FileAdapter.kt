package com.example.dadsad_freefilemanager

import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class FileAdapter(
    private val files: MutableList<FileItem>,
    private val onClick: (FileItem) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private var contextMenuPosition: Int = -1 // Track the position for context menu
    private var searchQuery: String = "" // Store the current search query for highlighting
    private var currentDirPath: String = "" // Store the current directory path

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
        val file = File(fileItem.path)

        // Highlight the matching portion of the name if there's a search query
        if (searchQuery.isNotEmpty()) {
            val nameLower = fileItem.name.lowercase(Locale.getDefault())
            val queryLower = searchQuery.lowercase(Locale.getDefault())
            val startIndex = nameLower.indexOf(queryLower)
            if (startIndex != -1) {
                val spannableString = SpannableString(fileItem.name)
                spannableString.setSpan(
                    BackgroundColorSpan(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_light)),
                    startIndex,
                    startIndex + queryLower.length,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                holder.name.text = spannableString
            } else {
                holder.name.text = fileItem.name
            }
        } else {
            holder.name.text = fileItem.name
        }

        holder.icon.setImageResource(
            if (fileItem.isDirectory) R.drawable.ic_folder else R.drawable.ic_file
        )

        // Show the full path in details if this is a search result from a subdirectory
        val details = if (file.parentFile?.absolutePath != currentDirPath) {
            "Path: ${fileItem.path}\n" + if (fileItem.isDirectory) {
                "Folder"
            } else {
                val size = file.length() / 1024 // Size in KB
                val lastModified = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    .format(file.lastModified())
                "Size: $size KB | Modified: $lastModified"
            }
        } else {
            if (fileItem.isDirectory) {
                "Folder"
            } else {
                val size = file.length() / 1024 // Size in KB
                val lastModified = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    .format(file.lastModified())
                "Size: $size KB | Modified: $lastModified"
            }
        }
        holder.details.text = details
    }

    override fun getItemCount(): Int = files.size

    fun getItemAtContextMenuPosition(): FileItem? {
        return if (contextMenuPosition != -1 && contextMenuPosition < files.size) {
            files[contextMenuPosition]
        } else {
            null
        }
    }

    fun setSearchQuery(query: String) {
        this.searchQuery = query
        notifyDataSetChanged()
    }

    fun setCurrentDirPath(path: String) {
        this.currentDirPath = path
        notifyDataSetChanged()
    }
}