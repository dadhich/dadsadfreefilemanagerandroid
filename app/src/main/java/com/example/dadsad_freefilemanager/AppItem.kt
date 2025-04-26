package com.example.dadsad_freefilemanager

import android.graphics.drawable.Drawable

data class AppItem(
    val name: String,
    val packageName: String,
    val size: Long,
    val icon: Drawable
)