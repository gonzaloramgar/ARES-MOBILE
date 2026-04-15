package com.ares.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ares.mobile.ai.ModelInstallState
import com.ares.mobile.ai.ModelPreference
import com.ares.mobile.ui.theme.BackgroundDeep
import com.ares.mobile.ui.theme.BorderSubtle
import com.ares.mobile.ui.theme.NeonRed
import com.ares.mobile.ui.theme.NeonRedBorder
import com.ares.mobile.ui.theme.NeonRedDim
import com.ares.mobile.ui.theme.SurfaceDark
import com.ares.mobile.ui.theme.SurfaceElevated
import com.ares.mobile.ui.theme.SurfaceVariantDark
import com.ares.mobile.ui.theme.TextMuted
import com.ares.mobile.ui.theme.TextPrimary
import com.ares.mobile.ui.theme.TextSecondary
import com.ares.mobile.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hfTokenInput by remember { mutableStateOf("") }
    var geminiKeyInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Configuración", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

        // ── Model selection ────────────────────────────────────────────
        AresCard(title = "Modelo") {
            ModelPreference.entries.forEach { preference ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(preference.name, color = TextPrimary, fontSize = 13.sp)
                        Text(
                            when (preference) {
                                ModelPreference.E2B -> "~1.5 GB · más ligero"
                                ModelPreference.E4B -> "~3 GB · más capaz"
                                else -> ""
                            },
                            color = TextSecondary,
                            fontSize = 11.sp,
                        )
                    }
                    RadioButton(
                        selected = state.modelPreference == preference,
                        onClick = { viewModel.setModelPreference(preference) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = NeonRed,
                            unselectedColor = TextMuted,
                        ),
                    )
                }
            }
        }

        // ── Thinking mode ──────────────────────────────────────────────
        AresCard(title = "Agente") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Thinking mode", color = TextPrimary, fontSize = 13.sp)
                    Text("Más deliberación antes de responder", color = TextSecondary, fontSize = 11.sp)
                }
                Switch(
                    checked = state.thinkingEnabled,
                    onCheckedChange = viewModel::setThinkingEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonRed,
                        checkedTrackColor = NeonRedDim,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = SurfaceElevated,
                    ),
                )
            }
        }

        // ── Model install ──────────────────────────────────────────────
        AresCard(title = "Instalación") {
            Text(
                text = when (val s = state.installState) {
                    is ModelInstallState.Missing -> "⬡ Pendiente · ${s.expectedPath}"
                    is ModelInstallState.Ready -> "✓ Listo · ${s.path}"
                    is ModelInstallState.Downloading -> "↓ ${s.variant.displayName} · ${s.progressPercent}%"
                    is ModelInstallState.Error -> "✗ ${s.message}"
                    ModelInstallState.Checking -> "… Verificando"
                },
                color = when (state.installState) {
                    is ModelInstallState.Ready -> NeonRed
                    is ModelInstallState.Error -> NeonRed.copy(alpha = 0.6f)
                    else -> TextSecondary
                },
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp,
            )
            AresFilledButton(
                label = if (state.isInstalling) "Instalando..." else "Descargar / reinstalar",
                enabled = !state.isInstalling,
            ) { viewModel.installSelectedModel(context) }
            AresFilledButton(
                label = "Eliminar modelo local",
                enabled = state.installState is ModelInstallState.Ready,
            ) { viewModel.removeInstalledModel() }
        }

        // ── Gemini API key ─────────────────────────────────────────────
        AresCard(title = "API de Gemini (online)") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (state.geminiKeyConfigured) "Clave configurada ✓" else "Sin clave — modo local",
                    color = if (state.geminiKeyConfigured) NeonRed else TextSecondary,
                    fontSize = 11.sp,
                )
                Text(
                    text = if (state.isOnline) "● Online" else "○ Offline",
                    color = if (state.isOnline) NeonRed else TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Text(
                "Con clave API usa Gemini Flash (gratis) para respuestas y análisis de imágenes. Sin ella solo funciona el modelo local.",
                color = TextMuted,
                fontSize = 10.sp,
                lineHeight = 14.sp,
            )
            BasicTextField(
                value = geminiKeyInput,
                onValueChange = { geminiKeyInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDeep, RoundedCornerShape(10.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                cursorBrush = SolidColor(NeonRed),
                singleLine = true,
                decorationBox = { inner ->
                    if (geminiKeyInput.isEmpty()) Text("AIza...", color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    inner()
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AresFilledButton("Guardar clave", modifier = Modifier.weight(1f)) {
                    viewModel.setGeminiApiKey(geminiKeyInput)
                    geminiKeyInput = ""
                }
                AresFilledButton("Quitar", modifier = Modifier.weight(1f)) {
                    viewModel.setGeminiApiKey("")
                    geminiKeyInput = ""
                }
            }
        }

        // ── HF token ──────────────────────────────────────────────────
        AresCard(title = "Token Hugging Face") {
            Text(
                if (state.hfTokenConfigured) "Token configurado ✓" else "Requerido para modelos privados",
                color = if (state.hfTokenConfigured) NeonRed else TextSecondary,
                fontSize = 11.sp,
            )
            BasicTextField(
                value = hfTokenInput,
                onValueChange = { hfTokenInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundDeep, RoundedCornerShape(10.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                cursorBrush = SolidColor(NeonRed),
                singleLine = true,
                decorationBox = { inner ->
                    if (hfTokenInput.isEmpty()) Text("hf_...", color = TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    inner()
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AresFilledButton("Guardar token", modifier = Modifier.weight(1f)) {
                    viewModel.setHfToken(hfTokenInput)
                    hfTokenInput = ""
                }
                AresFilledButton("Quitar", modifier = Modifier.weight(1f)) {
                    viewModel.setHfToken("")
                    hfTokenInput = ""
                }
            }
        }

        // ── Reset ──────────────────────────────────────────────────────
        AresFilledButton("Reabrir primer arranque") { viewModel.resetOnboarding() }
    }
}

@Composable
private fun AresCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceVariantDark, RoundedCornerShape(14.dp))
            .border(1.dp, NeonRedBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, color = NeonRed, fontSize = 10.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
        content()
    }
}

