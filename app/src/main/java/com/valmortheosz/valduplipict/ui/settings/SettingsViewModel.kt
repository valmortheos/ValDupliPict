package com.valmortheosz.valduplipict.ui.settings

import androidx.lifecycle.ViewModel
import com.valmortheosz.valduplipict.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _similarityThreshold = MutableStateFlow(settingsRepository.getSimilarityThreshold())
    val similarityThreshold: StateFlow<Float> = _similarityThreshold.asStateFlow()

    private val _useTrash = MutableStateFlow(settingsRepository.getUseTrash())
    val useTrash: StateFlow<Boolean> = _useTrash.asStateFlow()

    // Filter out implicit trash folder from UI to avoid confusion
    private val _excludedFolders = MutableStateFlow(
        settingsRepository.getExcludedFolders().filter { !it.endsWith("Trash") }
    )
    val excludedFolders: StateFlow<List<String>> = _excludedFolders.asStateFlow()

    fun updateThreshold(value: Float) {
        _similarityThreshold.value = value
        settingsRepository.setSimilarityThreshold(value)
    }

    fun updateUseTrash(value: Boolean) {
        _useTrash.value = value
        settingsRepository.setUseTrash(value)
    }

    fun addExcludedFolder(path: String) {
        if (path.isBlank()) return
        val currentList = settingsRepository.getExcludedFolders().toMutableSet()
        currentList.add(path)
        settingsRepository.setExcludedFolders(currentList)
        _excludedFolders.value = settingsRepository.getExcludedFolders().filter { !it.endsWith("Trash") }
    }

    fun removeExcludedFolder(path: String) {
        val currentList = settingsRepository.getExcludedFolders().toMutableSet()
        currentList.remove(path)
        settingsRepository.setExcludedFolders(currentList)
        _excludedFolders.value = settingsRepository.getExcludedFolders().filter { !it.endsWith("Trash") }
    }

    fun addQuickExclusions(paths: List<String>) {
        val currentList = settingsRepository.getExcludedFolders().toMutableSet()
        paths.forEach { path ->
            if (path.isNotBlank()) {
                currentList.add(path)
            }
        }
        settingsRepository.setExcludedFolders(currentList)
        _excludedFolders.value = settingsRepository.getExcludedFolders().filter { !it.endsWith("Trash") }
    }
}
