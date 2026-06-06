package com.valmortheosz.valduplipict.ui.duplicates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valmortheosz.valduplipict.data.model.DuplicateGroup
import com.valmortheosz.valduplipict.data.model.ImageFile
import com.valmortheosz.valduplipict.data.repository.DuplicateRepository
import com.valmortheosz.valduplipict.domain.usecase.DeleteDuplicatesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DuplicateListViewModel @Inject constructor(
    private val duplicateRepository: DuplicateRepository,
    private val deleteDuplicatesUseCase: DeleteDuplicatesUseCase
) : ViewModel() {

    val duplicateGroups: StateFlow<List<DuplicateGroup>> = duplicateRepository.duplicateGroups

    private val _selectedFiles = MutableStateFlow<Set<String>>(emptySet())
    val selectedFiles: StateFlow<Set<String>> = _selectedFiles.asStateFlow()

    fun toggleSelection(filePath: String) {
        val current = _selectedFiles.value.toMutableSet()
        if (current.contains(filePath)) {
            current.remove(filePath)
        } else {
            current.add(filePath)
        }
        _selectedFiles.value = current
    }

    fun deleteSelectedFiles(useTrash: Boolean = true) {
        viewModelScope.launch {
            val pathsToDelete = _selectedFiles.value
            if (pathsToDelete.isEmpty()) return@launch

            val filesToDelete = mutableListOf<ImageFile>()
            val currentGroups = duplicateRepository.getDuplicateGroups()

            for (group in currentGroups) {
                filesToDelete.addAll(group.files.filter { pathsToDelete.contains(it.filePath) })
            }

            val success = deleteDuplicatesUseCase(filesToDelete, useTrash)
            if (success) {
                // Update repository state
                val updatedGroups = currentGroups.mapNotNull { group ->
                    val remainingFiles = group.files.filterNot { pathsToDelete.contains(it.filePath) }
                    if (remainingFiles.size > 1) {
                        group.copy(files = remainingFiles)
                    } else {
                        null // Remove group if 1 or 0 files left
                    }
                }
                duplicateRepository.updateDuplicateGroups(updatedGroups)
                _selectedFiles.value = emptySet()
            }
        }
    }

    fun autoSelectSmart() {
        val groups = duplicateRepository.getDuplicateGroups()
        val toSelect = mutableSetOf<String>()

        for (group in groups) {
            // Sort by size ascending, then by last modified ascending
            val sortedFiles = group.files.sortedWith(
                compareBy<ImageFile> { it.fileSize }
                    .thenBy { it.lastModified }
            )
            // Select all except the "best" one (largest/newest which would be at the end if we did descending)
            // But we want to KEEP the best one, so we select the worst ones to DELETE.
            // Wait, we sorted ascending by size/date. So the smallest/oldest are first.
            // We want to delete the smallest/oldest. The best one is the last one.
            for (i in 0 until sortedFiles.size - 1) {
                toSelect.add(sortedFiles[i].filePath)
            }
        }
        _selectedFiles.value = toSelect
    }
}
