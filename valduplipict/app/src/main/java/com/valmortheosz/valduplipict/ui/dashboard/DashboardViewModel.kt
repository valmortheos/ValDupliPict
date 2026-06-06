package com.valmortheosz.valduplipict.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
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

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanRepository: ScanRepository,
    private val duplicateRepository: DuplicateRepository
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    val totalImageCount: StateFlow<Int> = scanRepository.getTotalImageCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val duplicateGroupCount: StateFlow<Int> = duplicateRepository.duplicateGroups
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalWastedSpace: StateFlow<Long> = duplicateRepository.duplicateGroups
        .map { groups -> groups.sumOf { it.totalWastedSpace } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun startScan() {
        val threshold = sharedPrefs.getFloat("similarityThreshold", 0.90f)
        val scanWorkRequest = OneTimeWorkRequestBuilder<ScanWorker>()
            .setInputData(workDataOf("similarityThreshold" to threshold))
            .build()
        WorkManager.getInstance(context).enqueue(scanWorkRequest)
    }
}
