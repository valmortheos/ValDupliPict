package com.valmortheosz.valduplipict.domain.usecase

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.content.ContentUris
import android.app.RecoverableSecurityException
import android.os.Build
import android.content.IntentSender
import com.valmortheosz.valduplipict.data.model.ImageFile
import com.valmortheosz.valduplipict.data.model.TrashedFile
import com.valmortheosz.valduplipict.data.db.TrashedFileDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class DeleteDuplicatesUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trashedFileDao: TrashedFileDao
) {
    suspend operator fun invoke(filesToDelete: List<ImageFile>, useTrash: Boolean = true): Boolean = withContext(Dispatchers.IO) {
        try {
            if (useTrash) {
                val trashDir = File(context.getExternalFilesDir(null), "Trash")
                if (!trashDir.exists()) {
                    trashDir.mkdirs()
                }

                for (img in filesToDelete) {
                    val file = File(img.filePath)
                    if (file.exists()) {
                        val newFileName = ".valduplipict_${file.name}"
                        val trashFile = File(trashDir, newFileName)

                        file.copyTo(trashFile, overwrite = true)
                        if(trashFile.exists() && trashFile.length() == file.length()) {
                             trashedFileDao.insertTrashedFile(
                                TrashedFile(
                                    originalPath = img.filePath,
                                    originalFilename = img.fileName,
                                    currentFilename = newFileName,
                                    trashPath = trashFile.absolutePath,
                                    mimeType = "image/*", // Defaulting, we can parse extension if needed
                                    fileSize = img.fileSize,
                                    deletedDate = System.currentTimeMillis(),
                                    originalModifiedDate = img.lastModified,
                                    width = img.width,
                                    height = img.height
                                )
                             )
                             // Fallback delete local, MediaStore handles scoped storage deletes usually by prompting
                             file.delete()

                             // Clean up mediastore index so it doesn't show up as grey boxes
                             try {
                                 context.contentResolver.delete(
                                     MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                     "${MediaStore.Images.Media.DATA} = ?",
                                     arrayOf(img.filePath)
                                 )
                             } catch(e: Exception) {
                                // If Android 11+ prevents delete without prompt, the file.delete() might have worked or not, this is a fallback
                             }
                        }
                    }
                }
            } else {
                for (img in filesToDelete) {
                    val file = File(img.filePath)
                    if (file.exists()) {
                        file.delete()
                        try {
                             context.contentResolver.delete(
                                 MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                 "${MediaStore.Images.Media.DATA} = ?",
                                 arrayOf(img.filePath)
                             )
                         } catch(e: Exception) {}
                    }
                }
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
