package com.valmortheosz.valduplipict.worker

import android.content.Context

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo

import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.valmortheosz.valduplipict.data.repository.DuplicateRepository
import com.valmortheosz.valduplipict.domain.usecase.ScanImagesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class ScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val scanImagesUseCase: ScanImagesUseCase,
    private val duplicateRepository: DuplicateRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        setForeground(buildForegroundInfo("Stage 1: Discovering Photos...", 0))
        try {
            val similarityThreshold = inputData.getFloat("similarityThreshold", 0.90f)

            val duplicateGroups = scanImagesUseCase.invoke(similarityThreshold) { state, processed, total, fileName, duplicates, spaceSaved, skipped, excludedFolders ->
                val pct = if (total > 0) (processed * 100 / total) else 0
                val title = when (state) {
                    "DISCOVERING" -> "Stage 1: Discovering Photos..."
                    "INDEXING" -> "Stage 1: Analyzing ${total} photos"
                    "FINALIZING" -> "Stage 2: Analyzing Duplicates..."
                    "COMPLETED" -> "Scan Complete: ${duplicates} duplicates found"
                    else -> "Scanning: $processed / $total files"
                }
                setForeground(buildForegroundInfo(title, pct))
                setProgressAsync(
                    workDataOf(
                        "state" to state,
                        "processed" to processed,
                        "total" to total,
                        "current_file" to fileName,
                        "duplicates" to duplicates,
                        "space_saved" to spaceSaved,
                        "algorithm" to "dHash"
                    )
                )
            }

            duplicateRepository.updateDuplicateGroups(duplicateGroups)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun buildForegroundInfo(text: String, progress: Int): ForegroundInfo {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("ValDupliPict")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setOngoing(true)
            .setProgress(100, progress, progress == 0)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(1001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(1001, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Scan Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Progress scan duplikat gambar" }
            (applicationContext.getSystemService(NotificationManager::class.java))
                .createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "scan_progress"
    }
}
