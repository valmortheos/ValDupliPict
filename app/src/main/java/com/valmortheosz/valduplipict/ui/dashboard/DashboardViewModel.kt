package com.valmortheosz.valduplipict.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

import androidx.work.ExistingWorkPolicy
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import kotlinx.coroutines.launch
import java.util.UUID

import com.valmortheosz.valduplipict.data.repository.DuplicateRepository
import com.valmortheosz.valduplipict.data.repository.ScanRepository
import com.valmortheosz.valduplipict.worker.ScanWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first



enum class ScanState {
    IDLE, DISCOVERING, INDEXING, HASHING, FINALIZING, COMPLETED, ERROR, CANCELLED
}

data class ScanProgress(
    val state: ScanState = ScanState.IDLE,
    val processedCount: Int = 0,
    val totalCount: Int = 0,
    val duplicatesFound: Int = 0,
    val spaceSavedBytes: Long = 0L,
    val currentFileName: String = "",
    val currentAlgorithm: String = "",
    val skippedFilesCount: Int = 0,
    val excludedFoldersCount: Int = 0
)

data class DashboardUiState(
    val totalImages: Int = 0,
    val duplicateGroups: Int = 0,
    val spaceSavedBytes: Long = 0L,
    val isScanning: Boolean = false,
    val showPermissionRationale: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanRepository: ScanRepository,
    private val duplicateRepository: DuplicateRepository
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _scanProgress = MutableStateFlow(ScanProgress())
    val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            // Check for existing running scans
            WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow("val_scan").collect { infos ->
                val activeInfo = infos.firstOrNull { !it.state.isFinished }
                if (activeInfo != null) {
                    _uiState.update { it.copy(isScanning = true) }
                    observeWorkProgress(activeInfo.id)
                }
            }
        }
    }

    fun startScan() {
        val threshold = sharedPrefs.getFloat("similarityThreshold", 0.90f)
        val request = OneTimeWorkRequestBuilder<ScanWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(workDataOf("similarityThreshold" to threshold))
            .addTag("valduplipict_scan")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "val_scan",
            ExistingWorkPolicy.REPLACE,
            request
        )

        observeWorkProgress(request.id)
        _uiState.update { it.copy(isScanning = true) }
    }

    fun cancelScan() {
        WorkManager.getInstance(context).cancelUniqueWork("val_scan")
        _scanProgress.update { it.copy(state = ScanState.CANCELLED) }
        _uiState.update { it.copy(isScanning = false) }
    }

    fun showPermissionRationale() {
        _uiState.update { it.copy(showPermissionRationale = true) }
    }

    private fun observeWorkProgress(id: UUID) {
        viewModelScope.launch {
            WorkManager.getInstance(context).getWorkInfoByIdFlow(id).collect { info ->
                info ?: return@collect
                val data = info.progress

                _scanProgress.update {
                    it.copy(
                        state = when (info.state) {
                            WorkInfo.State.ENQUEUED -> ScanState.INDEXING
                            WorkInfo.State.RUNNING -> {
                                val stateStr = data.getString("state") ?: ScanState.INDEXING.name
                                try {
                                    ScanState.valueOf(stateStr)
                                } catch (e: Exception) {
                                    ScanState.INDEXING
                                }
                            }
                            WorkInfo.State.SUCCEEDED -> ScanState.COMPLETED
                            WorkInfo.State.FAILED -> ScanState.ERROR
                            WorkInfo.State.CANCELLED -> ScanState.CANCELLED
                            else -> ScanState.IDLE
                        },
                        processedCount = data.getInt("processed", 0),
                        totalCount = data.getInt("total", 0),
                        duplicatesFound = data.getInt("duplicates", 0),
                        spaceSavedBytes = data.getLong("space_saved", 0L),
                        currentFileName = data.getString("current_file") ?: "",
                        currentAlgorithm = data.getString("algorithm") ?: ""
                    )
                }

                if (info.state.isFinished) {
                    _uiState.update { it.copy(isScanning = false) }
                    loadStats()
                }
            }
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            val total = scanRepository.getTotalImageCountFlow().first()
            _uiState.update { it.copy(totalImages = total) }
        }
        viewModelScope.launch {
            val groups = duplicateRepository.duplicateGroups.first()
            _uiState.update {
                it.copy(
                    duplicateGroups = groups.size,
                    spaceSavedBytes = groups.sumOf { g -> g.totalWastedSpace }
                )
            }
        }
    }
}
