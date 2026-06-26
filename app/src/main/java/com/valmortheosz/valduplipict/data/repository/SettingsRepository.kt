package com.valmortheosz.valduplipict.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    fun getSimilarityThreshold(): Float {
        return prefs.getFloat("similarity_threshold", 0.90f)
    }

    fun setSimilarityThreshold(threshold: Float) {
        prefs.edit().putFloat("similarity_threshold", threshold).apply()
    }

    fun getUseTrash(): Boolean {
        return prefs.getBoolean("use_trash", true)
    }

    fun setUseTrash(use: Boolean) {
        prefs.edit().putBoolean("use_trash", use).apply()
    }

    fun getExcludedFolders(): Set<String> {
        val exclusions = prefs.getStringSet("excluded_folders", emptySet()) ?: emptySet()
        // Implicitly always ignore the internal trash folder
        val trashFolder = context.getExternalFilesDir("Trash")?.absolutePath ?: ""
        return if (trashFolder.isNotEmpty()) {
            exclusions + setOf(trashFolder)
        } else {
            exclusions
        }
    }

    fun setExcludedFolders(folders: Set<String>) {
        // Remove the internal trash folder from the set before saving, as it's implicit
        val trashFolder = context.getExternalFilesDir("Trash")?.absolutePath ?: ""
        val foldersToSave = folders.filter { it != trashFolder }.toSet()
        prefs.edit().putStringSet("excluded_folders", foldersToSave).apply()
    }
}
