package com.valmortheosz.valduplipict.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _similarityThreshold = MutableStateFlow(sharedPrefs.getFloat("similarityThreshold", 0.90f))
    val similarityThreshold: StateFlow<Float> = _similarityThreshold.asStateFlow()

    private val _useTrash = MutableStateFlow(sharedPrefs.getBoolean("useTrash", true))
    val useTrash: StateFlow<Boolean> = _useTrash.asStateFlow()

    private val _excludedFolders = MutableStateFlow(sharedPrefs.getStringSet("excludedFolders", emptySet())?.toList() ?: emptyList())
    val excludedFolders: StateFlow<List<String>> = _excludedFolders.asStateFlow()

    fun updateThreshold(value: Float) {
        _similarityThreshold.value = value
        sharedPrefs.edit().putFloat("similarityThreshold", value).apply()
    }

    fun updateUseTrash(value: Boolean) {
        _useTrash.value = value
        sharedPrefs.edit().putBoolean("useTrash", value).apply()
    }

    fun addExcludedFolder(path: String) {
        if (path.isBlank()) return
        val currentList = _excludedFolders.value.toMutableSet()
        currentList.add(path)
        _excludedFolders.value = currentList.toList()
        sharedPrefs.edit().putStringSet("excludedFolders", currentList).apply()
    }

    fun removeExcludedFolder(path: String) {
        val currentList = _excludedFolders.value.toMutableSet()
        currentList.remove(path)
        _excludedFolders.value = currentList.toList()
        sharedPrefs.edit().putStringSet("excludedFolders", currentList).apply()
    }
}
