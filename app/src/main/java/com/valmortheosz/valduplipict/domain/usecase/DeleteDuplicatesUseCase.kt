package com.valmortheosz.valduplipict.domain.usecase

import android.content.Context
import android.os.Environment
import com.valmortheosz.valduplipict.data.model.ImageFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class DeleteDuplicatesUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(filesToDelete: List<ImageFile>, useTrash: Boolean = true): Boolean = withContext(Dispatchers.IO) {
        try {
            if (useTrash) {
                val trashDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), ".valduplipict_trash")
                if (!trashDir.exists()) {
                    trashDir.mkdirs()
                }

                for (img in filesToDelete) {
                    val file = File(img.filePath)
                    if (file.exists()) {
                        val safeOriginalPath = file.absolutePath.replace("/", "__")
                        val trashFile = File(trashDir, "${System.currentTimeMillis()}__${safeOriginalPath}")
                        file.renameTo(trashFile)
                    }
                }
            } else {
                for (img in filesToDelete) {
                    val file = File(img.filePath)
                    if (file.exists()) {
                        file.delete()
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
