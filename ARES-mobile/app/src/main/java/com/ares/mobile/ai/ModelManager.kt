package com.ares.mobile.ai

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

sealed interface ModelInstallState {
    data object Checking : ModelInstallState
    data class Missing(val variant: AresModelVariant, val expectedPath: String) : ModelInstallState
    data class Ready(val variant: AresModelVariant, val path: String, val bytes: Long) : ModelInstallState
    data class Downloading(val variant: AresModelVariant, val progressPercent: Int) : ModelInstallState
    data class Error(val message: String) : ModelInstallState
}

class ModelManager(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val modelRouter: ModelRouter,
) {
    private val transientInstallState = MutableStateFlow<ModelInstallState?>(null)

    companion object {
        val keyModelPreference = stringPreferencesKey("model_preference")
        val keyThinkingEnabled = booleanPreferencesKey("thinking_enabled")
        val keyFirstRunCompleted = booleanPreferencesKey("first_run_completed")
        val keyModelPath = stringPreferencesKey("model_path")
        val keyInstalledModelKey = stringPreferencesKey("installed_model_key")
        val keyExpectedSha256 = stringPreferencesKey("model_sha256")
        val keyHfAccessToken = stringPreferencesKey("hf_access_token")
        val keyGeminiApiKey = stringPreferencesKey("gemini_api_key")
    }

    val settingsFlow: Flow<ModelSettings> = dataStore.data.map { preferences ->
        ModelSettings(
            preference = preferences[keyModelPreference]?.let(ModelPreference::valueOf) ?: ModelPreference.AUTO,
            thinkingEnabled = preferences[keyThinkingEnabled] ?: false,
            firstRunCompleted = preferences[keyFirstRunCompleted] ?: false,
            installedModelKey = preferences[keyInstalledModelKey],
            modelPath = preferences[keyModelPath],
            expectedSha256 = preferences[keyExpectedSha256],
            hfAccessToken = preferences[keyHfAccessToken],
            geminiApiKey = preferences[keyGeminiApiKey],
        )
    }

    suspend fun setHfAccessToken(token: String) {
        dataStore.edit { preferences ->
            val trimmed = token.trim()
            if (trimmed.isBlank()) {
                preferences.remove(keyHfAccessToken)
            } else {
                preferences[keyHfAccessToken] = trimmed
            }
        }
    }

    suspend fun setGeminiApiKey(key: String) {
        dataStore.edit { preferences ->
            val trimmed = key.trim()
            if (trimmed.isBlank()) preferences.remove(keyGeminiApiKey)
            else preferences[keyGeminiApiKey] = trimmed
        }
    }

    suspend fun updatePreference(preference: ModelPreference) {
        dataStore.edit { it[keyModelPreference] = preference.name }
    }

    suspend fun setThinkingEnabled(enabled: Boolean) {
        dataStore.edit { it[keyThinkingEnabled] = enabled }
    }

    suspend fun setFirstRunCompleted(completed: Boolean) {
        dataStore.edit { it[keyFirstRunCompleted] = completed }
    }

    suspend fun registerInstalledModel(variant: AresModelVariant, path: String, sha256: String? = null) {
        dataStore.edit { preferences ->
            preferences[keyInstalledModelKey] = variant.key
            preferences[keyModelPath] = path
            if (sha256 != null) {
                preferences[keyExpectedSha256] = sha256
            }
        }
    }

    suspend fun clearInstalledModel() {
        dataStore.edit { preferences ->
            preferences.remove(keyInstalledModelKey)
            preferences.remove(keyModelPath)
            preferences.remove(keyExpectedSha256)
        }
    }

    suspend fun resolveDesiredVariant(): AresModelVariant {
        val settings = settingsFlowSnapshot()
        return modelRouter.decide(settings.preference).variant
    }

    fun observeInstallState(): Flow<ModelInstallState> = combine(settingsFlow, transientInstallState) { settings, transientState ->
        transientState ?: computeInstallState(settings)
    }

    fun installPreferredModel(): Flow<ModelInstallState> = flow {
        val settings = settingsFlowSnapshot()
        val variant = resolveDesiredVariant()
        val targetFile = defaultModelFile(variant)
        val tempFile = File(targetFile.parentFile, "${variant.taskFileName}.download")
        targetFile.parentFile?.mkdirs()

        transientInstallState.value = ModelInstallState.Downloading(variant, 0)
        emit(ModelInstallState.Downloading(variant, 0))

        try {
            var downloaded = false
            var lastFailure: String? = null

            for (url in variant.downloadUrls) {
                val result = downloadModelFile(
                    url = url,
                    variant = variant,
                    tempFile = tempFile,
                    hfAccessToken = settings.hfAccessToken,
                )
                if (result == null) {
                    downloaded = true
                    break
                }
                lastFailure = result
            }

            if (!downloaded) {
                throw IllegalStateException(lastFailure ?: "No se pudo descargar el modelo")
            }

            val expectedSha = variant.expectedSha256
            if (!expectedSha.isNullOrBlank()) {
                val actualSha = sha256(tempFile)
                if (!actualSha.equals(expectedSha, ignoreCase = true)) {
                    tempFile.delete()
                    throw IllegalStateException("SHA-256 no coincide para ${variant.displayName}")
                }
            }

            withContext(Dispatchers.IO) {
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                if (!tempFile.renameTo(targetFile)) {
                    throw IllegalStateException("No se pudo mover el modelo descargado a ${targetFile.absolutePath}")
                }
            }

            registerInstalledModel(
                variant = variant,
                path = targetFile.absolutePath,
                sha256 = variant.expectedSha256,
            )
            transientInstallState.value = null
            emit(ModelInstallState.Ready(variant, targetFile.absolutePath, targetFile.length()))
        } catch (error: Exception) {
            tempFile.delete()
            val state = ModelInstallState.Error(error.message ?: "Error desconocido al descargar el modelo")
            transientInstallState.value = state
            emit(state)
        }
    }

    private suspend fun downloadModelFile(
        url: String,
        variant: AresModelVariant,
        tempFile: File,
        hfAccessToken: String?,
    ): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 30_000
                connection.readTimeout = 60_000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "ARES-Mobile/1.0")
                if (!hfAccessToken.isNullOrBlank() && url.contains("huggingface.co", ignoreCase = true)) {
                    connection.setRequestProperty("Authorization", "Bearer $hfAccessToken")
                }
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    return@runCatching when (responseCode) {
                        401, 403 -> "Acceso denegado al modelo (HTTP $responseCode). Configura token de Hugging Face en Ajustes."
                        404 -> "Archivo de modelo no encontrado (HTTP 404)."
                        else -> "Descarga fallida: HTTP $responseCode"
                    }
                }

                val totalBytes = connection.contentLengthLong.takeIf { it > 0 } ?: variant.estimatedSizeBytes
                var downloadedBytes = 0L
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

                connection.inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            downloadedBytes += read
                            val progressPercent = if (totalBytes > 0) {
                                ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 99)
                            } else {
                                0
                            }
                            transientInstallState.value = ModelInstallState.Downloading(variant, progressPercent)
                        }
                    }
                }

                null
            }.getOrElse { error ->
                error.message ?: "Error de red durante la descarga"
            }
        }

    suspend fun clearInstalledModelFiles() {
        val variant = resolveDesiredVariant()
        val modelFile = defaultModelFile(variant)
        withContext(Dispatchers.IO) {
            if (modelFile.exists()) {
                modelFile.delete()
            }
        }
        clearInstalledModel()
        transientInstallState.value = null
    }

    fun verifyRegisteredModel(): Flow<ModelInstallState> = flow {
        emit(ModelInstallState.Checking)
        val settings = settingsFlowSnapshot()
        val variant = modelRouter.decide(settings.preference).variant
        val modelFile = settings.modelPath?.let(::File) ?: defaultModelFile(variant)
        if (!modelFile.exists()) {
            emit(ModelInstallState.Missing(variant, modelFile.absolutePath))
            return@flow
        }
        val expectedSha = settings.expectedSha256
        if (expectedSha != null) {
            val actualSha = sha256(modelFile)
            if (!actualSha.equals(expectedSha, ignoreCase = true)) {
                emit(ModelInstallState.Error("SHA-256 mismatch for ${modelFile.name}"))
                return@flow
            }
        }
        emit(ModelInstallState.Ready(variant, modelFile.absolutePath, modelFile.length()))
    }

    fun defaultModelFile(variant: AresModelVariant): File =
        File(File(context.filesDir, "models"), variant.taskFileName)

    suspend fun modelFileForSelectedVariant(): File = defaultModelFile(resolveDesiredVariant())

    private fun computeInstallState(settings: ModelSettings): ModelInstallState {
        val expectedVariant = modelRouter.decide(settings.preference).variant
        val configuredFile = settings.modelPath?.takeIf { it.isNotBlank() }?.let(::File)
        val defaultFile = defaultModelFile(expectedVariant)
        val file = configuredFile ?: defaultFile
        return when {
            file.exists() -> ModelInstallState.Ready(
                variant = expectedVariant,
                path = file.absolutePath,
                bytes = file.length(),
            )

            else -> ModelInstallState.Missing(
                variant = expectedVariant,
                expectedPath = file.absolutePath,
            )
        }
    }

    private suspend fun settingsFlowSnapshot(): ModelSettings =
        settingsFlow.first()

    suspend fun sha256(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val bytesRead = input.read(buffer)
                if (bytesRead <= 0) break
                digest.update(buffer, 0, bytesRead)
            }
        }
        digest.digest().joinToString(separator = "") { "%02x".format(it) }
    }
}

data class ModelSettings(
    val preference: ModelPreference = ModelPreference.AUTO,
    val thinkingEnabled: Boolean = false,
    val firstRunCompleted: Boolean = false,
    val installedModelKey: String? = null,
    val modelPath: String? = null,
    val expectedSha256: String? = null,
    val hfAccessToken: String? = null,
    val geminiApiKey: String? = null,
)