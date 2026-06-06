package com.valmortheosz.valduplipict.worker

import android.content.Context
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
        try {
            val similarityThreshold = inputData.getFloat("similarityThreshold", 0.90f)

            val duplicateGroups = scanImagesUseCase.invoke(similarityThreshold) { progress, total ->
                setProgressAsync(workDataOf("progress" to progress, "total" to total))
            }

            duplicateRepository.updateDuplicateGroups(duplicateGroups)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
