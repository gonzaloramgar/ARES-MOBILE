package com.ares.mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ares.mobile.ai.ModelInstallState
import com.ares.mobile.ai.ModelManager
import com.ares.mobile.ai.ModelPreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val modelPreference: ModelPreference = ModelPreference.AUTO,
    val thinkingEnabled: Boolean = false,
    val firstRunCompleted: Boolean = false,
    val installState: ModelInstallState = ModelInstallState.Checking,
    val isInstalling: Boolean = false,
    val hfTokenConfigured: Boolean = false,
)

class SettingsViewModel(
    private val modelManager: ModelManager,
) : ViewModel() {
    private val isInstalling = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        modelManager.settingsFlow,
        modelManager.observeInstallState(),
        isInstalling,
    ) { settings, installState, installing ->
        SettingsUiState(
            modelPreference = settings.preference,
            thinkingEnabled = settings.thinkingEnabled,
            firstRunCompleted = settings.firstRunCompleted,
            installState = installState,
            isInstalling = installing,
            hfTokenConfigured = !settings.hfAccessToken.isNullOrBlank(),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState(),
    )

    fun setModelPreference(preference: ModelPreference) {
        viewModelScope.launch {
            modelManager.updatePreference(preference)
        }
    }

    fun setThinkingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            modelManager.setThinkingEnabled(enabled)
        }
    }

    fun markFirstRunCompleted() {
        viewModelScope.launch {
            modelManager.setFirstRunCompleted(true)
        }
    }

    fun installSelectedModel() {
        viewModelScope.launch {
            isInstalling.value = true
            try {
                modelManager.installPreferredModel().collect { }
            } finally {
                isInstalling.value = false
            }
        }
    }

    fun removeInstalledModel() {
        viewModelScope.launch {
            modelManager.clearInstalledModelFiles()
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            modelManager.setFirstRunCompleted(false)
        }
    }

    fun setHfToken(token: String) {
        viewModelScope.launch {
            modelManager.setHfAccessToken(token)
        }
    }
}