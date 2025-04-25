package com.example.dadsad_freefilemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dadsad_freefilemanager.R
import android.view.animation.AnimationUtils

class FileAdapter(
    private val fileList: List<FileItem>,
    private val onItemClick: (FileItem) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    private var contextMenuPosition: Int = -1

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileIcon: ImageView = itemView.findViewById(R.id.fileIcon)
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
        holder.fileIcon.setImageResource(
            if (fileItem.isDirectory) R.drawable.ic_folder else R.drawable.ic_file
        )
        holder.fileNameTextView.text = fileItem.name
        holder.fileTypeTextView.text = if (fileItem.isDirectory) "Folder" else "File"
        holder.itemView.setOnClickListener {
            onItemClick(fileItem)
        }
        holder.itemView.setOnLongClickListener {
            contextMenuPosition = position
            false
        }
        // Apply fade-in animation
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_in)
        holder.itemView.startAnimation(animation)
    }

    override fun getItemCount(): Int = fileList.size

    fun getItemAtContextMenuPosition(): FileItem? {
        return if (contextMenuPosition >= 0 && contextMenuPosition < fileList.size) {
            fileList[contextMenuPosition]
        } else {
            null
        }
    }
}