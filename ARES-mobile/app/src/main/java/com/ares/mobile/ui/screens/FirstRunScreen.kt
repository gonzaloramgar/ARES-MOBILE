package com.ares.mobile.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ares.mobile.ai.ModelInstallState
import com.ares.mobile.ui.theme.BackgroundDeep
import com.ares.mobile.ui.theme.BorderSubtle
import com.ares.mobile.ui.theme.NeonRed
import com.ares.mobile.ui.theme.NeonRedBorder
import com.ares.mobile.ui.theme.NeonRedDim
import com.ares.mobile.ui.theme.SurfaceDark
import com.ares.mobile.ui.theme.SurfaceVariantDark
import com.ares.mobile.ui.theme.TextAres
import com.ares.mobile.ui.theme.TextMuted
import com.ares.mobile.ui.theme.TextPrimary
import com.ares.mobile.ui.theme.TextSecondary
import com.ares.mobile.viewmodel.SettingsViewModel

private val requestedPermissions = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.POST_NOTIFICATIONS,
)

@Composable
fun FirstRunScreen(
    onContinue: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // ── Logo ───────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.linearGradient(listOf(NeonRed, NeonRedDim)),
                            RoundedCornerShape(18.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("⬡", fontSize = 30.sp)
                }
                Text(
                    "ARES",
                    color = NeonRed,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp,
                )
                Text(
                    "Asistente IA on-device con Gemma 4",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }

            // ── Model status card ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceVariantDark, RoundedCornerShape(14.dp))
                    .border(1.dp, NeonRedBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "Estado del modelo",
                    color = NeonRed,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace,
                )

                if (state.installState is ModelInstallState.Downloading) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(
                            progress = { (state.installState as ModelInstallState.Downloading).progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(3.dp),
                            color = NeonRed,
                            trackColor = SurfaceDark,
                        )
                        Text(
                            "${(state.installState as ModelInstallState.Downloading).progressPercent}%",
                            color = TextAres,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }

                Text(
                    text = when (val s = state.installState) {
                        is ModelInstallState.Missing -> "⬡ Falta: ${s.variant.displayName}\n${s.expectedPath}"
                        is ModelInstallState.Ready -> "✓ Modelo listo · ${s.path}"
                        is ModelInstallState.Downloading -> "↓ Descargando ${s.variant.displayName}..."
                        is ModelInstallState.Error -> "✗ ${s.message}"
                        ModelInstallState.Checking -> "… Verificando instalación"
                    },
                    color = when (state.installState) {
                        is ModelInstallState.Ready -> NeonRed
                        is ModelInstallState.Error -> NeonRed.copy(alpha = 0.7f)
                        else -> TextSecondary
                    },
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp,
                )
            }

            // ── Permissions list ───────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(14.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Permisos requeridos", color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
                listOf(
                    "📷  Cámara — análisis de imágenes",
                    "🎤  Micrófono — comandos de voz",
                    "📍  Ubicación — herramienta GPS",
                    "🔔  Notificaciones — alarmas y recordatorios",
                ).forEach {
                    Text(it, color = TextSecondary, fontSize = 12.sp)
                }
            }

            // ── Buttons ────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AresOutlinedButton("Conceder permisos") {
                    permissionLauncher.launch(requestedPermissions)
                }
                AresFilledButton(
                    label = if (state.isInstalling) "Descargando..." else "Descargar modelo Gemma 4",
                    enabled = !state.isInstalling,
                ) {
                    viewModel.installSelectedModel()
                }
                AresFilledButton(
                    label = if (state.installState is ModelInstallState.Ready) "Empezar →" else "Continuar sin modelo",
                ) {
                    viewModel.markFirstRunCompleted()
                    onContinue()
                }
            }
        }
    }
}

@Composable
fun AresFilledButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NeonRedDim,
            contentColor = TextAres,
            disabledContainerColor = SurfaceDark,
            disabledContentColor = TextMuted,
        ),
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun AresOutlinedButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = NeonRed,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonRedBorder),
    ) {
        Text(label, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}
