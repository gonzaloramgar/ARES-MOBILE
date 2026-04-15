package com.ares.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ares.mobile.AppContainer

class AresViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(
                    conversationHistory = container.conversationHistory,
                    agentLoop = container.agentLoop,
                    modelManager = container.modelManager,
                ) as T
            }

            modelClass.isAssignableFrom(MemoryViewModel::class.java) -> {
                MemoryViewModel(memoryDao = container.memoryDao) as T
            }

            modelClass.isAssignableFrom(TasksViewModel::class.java) -> {
                TasksViewModel(scheduledTaskDao = container.scheduledTaskDao) as T
            }

            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    modelManager = container.modelManager,
                    networkChecker = container.networkChecker,
                ) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
