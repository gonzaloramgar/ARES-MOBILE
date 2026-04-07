package com.ares.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ares.mobile.data.ScheduledTaskDao
import com.ares.mobile.data.ScheduledTaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(
    private val scheduledTaskDao: ScheduledTaskDao,
) : ViewModel() {
    val tasks: StateFlow<List<ScheduledTaskEntity>> = scheduledTaskDao.observeTasks()
        .map { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            scheduledTaskDao.deleteById(id)
        }
    }
}