package com.valmortheosz.valduplipict.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_files")
data class ImageFile(
    @PrimaryKey val filePath: String,
    val fileName: String,
    val folderName: String,
    val fileSize: Long,
    val lastModified: Long,
    val width: Int,
    val height: Int,
    val md5Hash: String? = null,
    val dHash: Long? = null,
    val pHash: Long? = null,
    val aHash: Long? = null
)
