package com.valmortheosz.valduplipict.data.repository

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.valmortheosz.valduplipict.data.db.ImageDao
import com.valmortheosz.valduplipict.data.model.ImageFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageDao: ImageDao
) {
    suspend fun getAllImagesFromMediaStore(): List<ImageFile> = withContext(Dispatchers.IO) {
        val images = mutableListOf<ImageFile>()
        val projection = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val path = cursor.getString(dataColumn)
                if (path == null) continue

                val file = File(path)
                if (!file.exists()) continue

                val name = cursor.getString(nameColumn) ?: file.name
                val size = cursor.getLong(sizeColumn)
                val dateModified = cursor.getLong(dateColumn)
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val folderName = cursor.getString(bucketColumn) ?: file.parentFile?.name ?: "Unknown"

                images.add(
                    ImageFile(
                        filePath = path,
                        fileName = name,
                        folderName = folderName,
                        fileSize = size,
                        lastModified = dateModified,
                        width = width,
                        height = height
                    )
                )
            }
        }
        return@withContext images
    }

    suspend fun getCachedImages(): List<ImageFile> {
        return imageDao.getAllImages()
    }

    suspend fun saveImagesToCache(images: List<ImageFile>) {
        imageDao.insertAll(images)
    }

    suspend fun clearCache() {
        imageDao.clearAll()
    }

    fun getTotalImageCountFlow(): Flow<Int> {
        return imageDao.getTotalImageCountFlow()
    }
}
