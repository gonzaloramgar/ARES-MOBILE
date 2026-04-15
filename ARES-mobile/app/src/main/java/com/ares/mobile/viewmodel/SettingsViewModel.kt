package com.ares.mobile.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ares.mobile.ai.ModelDownloadService
import com.ares.mobile.ai.ModelInstallState
import com.ares.mobile.ai.ModelManager
import com.ares.mobile.ai.ModelPreference
import com.ares.mobile.ai.NetworkChecker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    val geminiKeyConfigured: Boolean = false,
    val isOnline: Boolean = false,
)

class SettingsViewModel(
    private val modelManager: ModelManager,
    private val networkChecker: NetworkChecker,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        modelManager.settingsFlow,
        modelManager.observeInstallState(),
    ) { settings, installState ->
        SettingsUiState(
            modelPreference = settings.preference,
            thinkingEnabled = settings.thinkingEnabled,
            firstRunCompleted = settings.firstRunCompleted,
            installState = installState,
            isInstalling = installState is ModelInstallState.Downloading,
            hfTokenConfigured = !settings.hfAccessToken.isNullOrBlank(),
            geminiKeyConfigured = !settings.geminiApiKey.isNullOrBlank(),
            isOnline = networkChecker.isOnline(),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState(),
    )

    fun setModelPreference(preference: ModelPreference) {
        viewModelScope.launch { modelManager.updatePreference(preference) }
    }

    fun setThinkingEnabled(enabled: Boolean) {
        viewModelScope.launch { modelManager.setThinkingEnabled(enabled) }
    }

    fun markFirstRunCompleted() {
        viewModelScope.launch { modelManager.setFirstRunCompleted(true) }
    }

    /** Starts the download as a Foreground Service — survives app backgrounding. */
    fun installSelectedModel(context: Context) {
        val intent = Intent(context.applicationContext, ModelDownloadService::class.java)
        ContextCompat.startForegroundService(context.applicationContext, intent)
    }

    fun removeInstalledModel() {
        viewModelScope.launch { modelManager.clearInstalledModelFiles() }
    }

    fun resetOnboarding() {
        viewModelScope.launch { modelManager.setFirstRunCompleted(false) }
    }

    fun setHfToken(token: String) {
        viewModelScope.launch { modelManager.setHfAccessToken(token) }
    }

    fun setGeminiApiKey(key: String) {
        viewModelScope.launch { modelManager.setGeminiApiKey(key) }
    }
}
