package com.valmortheosz.valduplipict.ui.trash

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val trashDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), ".valduplipict_trash")

    private val _trashFiles = MutableStateFlow<List<File>>(emptyList())
    val trashFiles: StateFlow<List<File>> = _trashFiles.asStateFlow()

    init {
        loadTrashFiles()
    }

    private fun loadTrashFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            if (trashDir.exists()) {
                val files = trashDir.listFiles()?.toList() ?: emptyList()
                _trashFiles.value = files.sortedByDescending { it.lastModified() }
            } else {
                _trashFiles.value = emptyList()
            }
        }
    }

    fun restoreFile(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // The original name is after the first underscore: timestamp_originalName
                val originalName = file.name.substringAfter("_")

                // Assuming original path was in Pictures for simplicity, or we can use MediaStore to find the right place.
                // A better approach would be storing original paths in DB, but for now we restore to Pictures.
                val restoreDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val restoredFile = File(restoreDir, originalName)

                if (file.renameTo(restoredFile)) {
                    loadTrashFiles()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteFilePermanently(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            if (file.delete()) {
                loadTrashFiles()
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            if (trashDir.exists()) {
                trashDir.listFiles()?.forEach { it.delete() }
                loadTrashFiles()
            }
        }
    }
}
