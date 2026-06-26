package com.valmortheosz.valduplipict.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trashed_files")
data class TrashedFile(
    @PrimaryKey
    val originalPath: String,
    val originalFilename: String,
    val currentFilename: String,
    val trashPath: String,
    val mimeType: String,
    val fileSize: Long,
    val deletedDate: Long,
    val originalModifiedDate: Long,
    val width: Int,
    val height: Int
)
