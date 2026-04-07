package com.ares.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ares.mobile.data.MemoryDao
import com.ares.mobile.data.MemoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MemoryItemUi(
    val key: String,
    val value: String,
    val updatedAt: Long,
)

class MemoryViewModel(
    private val memoryDao: MemoryDao,
) : ViewModel() {
    val memories: StateFlow<List<MemoryItemUi>> = memoryDao.observeMemories()
        .map { items -> items.map { MemoryItemUi(it.key, it.value, it.updatedAt) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveMemory(key: String, value: String) {
        if (key.isBlank() || value.isBlank()) return
        viewModelScope.launch {
            memoryDao.upsert(
                MemoryEntity(
                    key = key.trim(),
                    value = value.trim(),
                    updatedAt = System.currentTimeMillis(),
                )
            )
        }
    }

    fun deleteMemory(key: String) {
        viewModelScope.launch {
            memoryDao.deleteByKey(key)
        }
    }
}