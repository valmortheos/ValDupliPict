package com.valmortheosz.valduplipict.data.repository

import com.valmortheosz.valduplipict.data.model.DuplicateGroup
import com.valmortheosz.valduplipict.data.model.ImageFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DuplicateRepository @Inject constructor() {
    private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<DuplicateGroup>> = _duplicateGroups.asStateFlow()

    fun updateDuplicateGroups(groups: List<DuplicateGroup>) {
        _duplicateGroups.value = groups
    }

    fun getDuplicateGroups(): List<DuplicateGroup> {
        return _duplicateGroups.value
    }

    fun removeGroup(groupId: String) {
        val currentGroups = _duplicateGroups.value.toMutableList()
        currentGroups.removeAll { it.groupId == groupId }
        _duplicateGroups.value = currentGroups
    }
}
