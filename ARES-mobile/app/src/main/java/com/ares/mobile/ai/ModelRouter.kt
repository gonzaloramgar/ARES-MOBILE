package com.ares.mobile.ai

import android.app.ActivityManager
import android.content.Context

enum class ModelPreference {
    AUTO,
    E2B,
    E4B,
}

enum class AresModelVariant(
    val key: String,
    val displayName: String,
    val taskFileName: String,
    val downloadUrls: List<String>,
    val expectedSha256: String?,
    val estimatedSizeBytes: Long,
) {
    E2B(
        key = "gemma-4-e2b",
        displayName = "Gemma 4 E2B",
        taskFileName = "gemma-4-E2B-it.litertlm",
        downloadUrls = listOf(
            "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm?download=true",
            "https://huggingface.co/google/gemma-3n-E2B-it-litert-lm/resolve/main/gemma-3n-E2B-it-int4.litertlm?download=true",
            "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.litertlm?download=true",
        ),
        expectedSha256 = null,
        estimatedSizeBytes = 1_200_000_000L,
    ),
    E4B(
        key = "gemma-4-e4b",
        displayName = "Gemma 4 E4B",
        taskFileName = "gemma-4-E4B-it.litertlm",
        downloadUrls = listOf(
            "https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm?download=true",
            "https://huggingface.co/google/gemma-3n-E4B-it-litert-lm/resolve/main/gemma-3n-E4B-it-int4.litertlm?download=true",
            "https://huggingface.co/litert-community/Gemma3-1B-IT/resolve/main/gemma3-1b-it-int4.task?download=true",
        ),
        expectedSha256 = null,
        estimatedSizeBytes = 2_400_000_000L,
    ),
}

data class ModelRouteDecision(
    val variant: AresModelVariant,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
)

class ModelRouter(
    private val context: Context,
) {
    companion object {
        fun chooseAutomaticVariant(totalRamBytes: Long, availableRamBytes: Long): AresModelVariant {
            val totalRamGb = totalRamBytes.toDouble() / 1024 / 1024 / 1024
            val availableRamGb = availableRamBytes.toDouble() / 1024 / 1024 / 1024
            return if (availableRamGb >= 4.0 || totalRamGb >= 8.0) AresModelVariant.E4B else AresModelVariant.E2B
        }
    }

    fun decide(preference: ModelPreference = ModelPreference.AUTO): ModelRouteDecision {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val variant = when (preference) {
            ModelPreference.E2B -> AresModelVariant.E2B
            ModelPreference.E4B -> AresModelVariant.E4B
            ModelPreference.AUTO -> chooseAutomatic(
                totalRamBytes = memoryInfo.totalMem,
                availableRamBytes = memoryInfo.availMem,
            )
        }

        return ModelRouteDecision(
            variant = variant,
            totalRamBytes = memoryInfo.totalMem,
            availableRamBytes = memoryInfo.availMem,
        )
    }

    fun chooseAutomatic(totalRamBytes: Long, availableRamBytes: Long): AresModelVariant {
        return chooseAutomaticVariant(totalRamBytes, availableRamBytes)
    }
}