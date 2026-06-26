package com.valmortheosz.valduplipict.ui.trash

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valmortheosz.valduplipict.data.db.TrashedFileDao
import com.valmortheosz.valduplipict.data.model.TrashedFile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val trashedFileDao: TrashedFileDao
) : ViewModel() {

    private val _trashFiles = MutableStateFlow<List<TrashedFile>>(emptyList())
    val trashFiles: StateFlow<List<TrashedFile>> = _trashFiles.asStateFlow()

    init {
        loadTrashFiles()
    }

    private fun loadTrashFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            trashedFileDao.getAllTrashedFiles().collectLatest { files ->
                _trashFiles.value = files
            }
        }
    }

    fun restoreFile(trashedFile: TrashedFile) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileInTrash = File(trashedFile.trashPath)
                val originalFile = File(trashedFile.originalPath)

                // Ensure parent exists
                originalFile.parentFile?.mkdirs()

                if (fileInTrash.exists()) {
                    fileInTrash.copyTo(originalFile, overwrite = true)
                    fileInTrash.delete()
                }

                trashedFileDao.deleteTrashedFile(trashedFile)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteFilePermanently(trashedFile: TrashedFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileInTrash = File(trashedFile.trashPath)
            if (fileInTrash.exists()) {
                fileInTrash.delete()
            }
            trashedFileDao.deleteTrashedFile(trashedFile)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            val files = trashedFileDao.getAllTrashedFilesList()
            files.forEach { trashedFile ->
                val fileInTrash = File(trashedFile.trashPath)
                if (fileInTrash.exists()) {
                    fileInTrash.delete()
                }
            }
            trashedFileDao.clearAll()
        }
    }
}
