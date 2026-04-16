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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ares.mobile.ai.ModelInstallState
import com.ares.mobile.ai.ModelPreference
import com.ares.mobile.ui.components.AresTabHeroHeader
import com.ares.mobile.ui.components.ConfirmationDialog
import com.ares.mobile.ui.components.ModelTutorialDialog
import com.ares.mobile.ui.components.TutorialStep
import com.ares.mobile.ui.theme.SurfaceElevated
import com.ares.mobile.ui.theme.NeonRed
import com.ares.mobile.ui.theme.NeonRedDim
import com.ares.mobile.ui.theme.AresThemeVibrant
import com.ares.mobile.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    AresThemeVibrant {
        SettingsScreenContent(viewModel)
    }
}

@Composable
private fun SettingsScreenContent(viewModel: SettingsViewModel) {
    val colors = MaterialTheme.colorScheme
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hfTokenInput by remember { mutableStateOf("") }
    var geminiKeyInput by remember { mutableStateOf("") }
    var showRemoveModelConfirmation by remember { mutableStateOf(false) }
    var showRemoveGeminiKeyConfirmation by remember { mutableStateOf(false) }
    var showRemoveHfTokenConfirmation by remember { mutableStateOf(false) }

    // Tutorial dialogs
    var showGwopusTutorial by remember { mutableStateOf(false) }
    var showGeminiTutorial by remember { mutableStateOf(false) }
    var showHfTutorial by remember { mutableStateOf(false) }
    var showThinkingTutorial by remember { mutableStateOf(false) }

    // Confirmation dialogs
    ConfirmationDialog(
        isVisible = showRemoveModelConfirmation,
        title = "Eliminar modelo local",
        message = "¿Eliminar el modelo de IA descargado? Podrás descargarlo de nuevo después.",
        confirmButtonText = "Eliminar",
        dismissButtonText = "Cancelar",
        isDestructive = true,
        onConfirm = {
            viewModel.removeInstalledModel()
            showRemoveModelConfirmation = false
        },
        onDismiss = { showRemoveModelConfirmation = false },
    )

    ConfirmationDialog(
        isVisible = showRemoveGeminiKeyConfirmation,
        title = "Quitar clave Gemini",
        message = "¿Eliminar la clave API de Gemini? Solo funcionará el modelo local.",
        confirmButtonText = "Quitar",
        dismissButtonText = "Cancelar",
        isDestructive = true,
        onConfirm = {
            viewModel.setGeminiApiKey("")
            geminiKeyInput = ""
            showRemoveGeminiKeyConfirmation = false
        },
        onDismiss = { showRemoveGeminiKeyConfirmation = false },
    )

    ConfirmationDialog(
        isVisible = showRemoveHfTokenConfirmation,
        title = "Quitar token Hugging Face",
        message = "¿Eliminar el token de Hugging Face? No podrás usar modelos privados.",
        confirmButtonText = "Quitar",
        dismissButtonText = "Cancelar",
        isDestructive = true,
        onConfirm = {
            viewModel.setHfToken("")
            hfTokenInput = ""
            showRemoveHfTokenConfirmation = false
        },
        onDismiss = { showRemoveHfTokenConfirmation = false },
    )

    // Tutorial dialogs
    ModelTutorialDialog(
        isVisible = showGwopusTutorial,
        title = "Configurar GWOPUS 3.5",
        steps = listOf(
            TutorialStep(
                title = "Obtén acceso a Hugging Face",
                description = "Necesitas una cuenta en Hugging Face para usar este modelo online.",
                details = listOf(
                    "Visita huggingface.co y crea una cuenta gratis",
                    "Verifica tu correo electrónico",
                    "Asegúrate de estar en línea (requiere internet)",
                ),
            ),
            TutorialStep(
                title = "Genera tu token de acceso",
                description = "El token permite a ARES acceder a los modelos privados.",
                details = listOf(
                    "En tu perfil, ve a Settings → Access Tokens",
                    "Haz clic en 'New token' y selecciona 'Read'",
                    "Copia el token (comienza con 'hf_')",
                ),
            ),
            TutorialStep(
                title = "Configura el token en ARES",
                description = "Pega el token en la sección de Token Hugging Face.",
                details = listOf(
                    "Baja a la sección 'Token Hugging Face'",
                    "Pega tu token en el campo de texto",
                    "Presiona 'Guardar token'",
                ),
            ),
            TutorialStep(
                title = "Activa GWOPUS 3.5",
                description = "Ahora podrás usar GWOPUS 3.5 como tu modelo de IA.",
                details = listOf(
                    "Presiona 'Activar GWOPUS 3.5 online'",
                    "ARES usará modelos Qwen con estilo conversacional tipo Opus",
                    "Requiere conexión a internet para cada respuesta",
                ),
            ),
        ),
        onDismiss = { showGwopusTutorial = false },
    )

    ModelTutorialDialog(
        isVisible = showGeminiTutorial,
        title = "Configurar API de Gemini",
        steps = listOf(
            TutorialStep(
                title = "Accede a Google AI Studio",
                description = "Crea una clave API de Gemini de forma gratuita.",
                details = listOf(
                    "Visita aistudio.google.com",
                    "Haz clic en 'Get API Key' en el menú lateral",
                    "Selecciona 'Create API Key'",
                ),
            ),
            TutorialStep(
                title = "Copia tu clave API",
                description = "Necesitarás esta clave para acceder a Gemini Flash.",
                details = listOf(
                    "Se generará una clave que comienza con 'AIza'",
                    "Cópiala haciendo clic en el icono de copiar",
                    "Mantén esta clave privada",
                ),
            ),
            TutorialStep(
                title = "Pega la clave en ARES",
                description = "Configura la API de Gemini para mejores respuestas.",
                details = listOf(
                    "En la sección 'API de Gemini (online)', pega tu clave",
                    "Presiona 'Guardar clave'",
                    "ARES usará Gemini Flash automáticamente cuando tengas internet",
                ),
            ),
            TutorialStep(
                title = "Beneficios",
                description = "Con Gemini activo disfrutarás de capacidades mejoradas.",
                details = listOf(
                    "Respuestas más inteligentes y contextuales",
                    "Análisis de imágenes (si se implementa)",
                    "Mejor comprensión del lenguaje natural",
                    "Sin costo adicional (Gemini Flash es gratis)",
                ),
            ),
        ),
        onDismiss = { showGeminiTutorial = false },
    )

    ModelTutorialDialog(
        isVisible = showHfTutorial,
        title = "Configurar Token Hugging Face",
        steps = listOf(
            TutorialStep(
                title = "¿Por qué necesitas un token?",
                description = "El token permite acceso a modelos privados y gated de Hugging Face.",
                details = listOf(
                    "Algunos modelos requieren aceptar términos",
                    "El token verifica que has aceptado los términos",
                    "Es gratis crear un token",
                ),
            ),
            TutorialStep(
                title = "Crea tu cuenta de Hugging Face",
                description = "Si no tienes cuenta, créala ahora.",
                details = listOf(
                    "Visita huggingface.co",
                    "Haz clic en 'Sign Up'",
                    "Completa el registro y verifica tu email",
                ),
            ),
            TutorialStep(
                title = "Genera un nuevo token",
                description = "Crea un token de lectura para ARES.",
                details = listOf(
                    "Ve a tu perfil → Settings → Access Tokens",
                    "Presiona 'New token'",
                    "Selecciona 'Read' como tipo",
                    "Haz clic en 'Generate'",
                ),
            ),
            TutorialStep(
                title = "Configura en ARES",
                description = "Pega el token para habilitar acceso a modelos gated.",
                details = listOf(
                    "Copia el token (comienza con 'hf_')",
                    "En ARES, pega en 'Token Hugging Face'",
                    "Presiona 'Guardar token'",
                    "Ahora puedes usar GWOPUS u otros modelos privados",
                ),
            ),
        ),
        onDismiss = { showHfTutorial = false },
    )

    ModelTutorialDialog(
        isVisible = showThinkingTutorial,
        title = "Razonamiento (Thinking Mode)",
        steps = listOf(
            TutorialStep(
                title = "¿Qué es el razonamiento?",
                description = "Un modo que hace pensar más al modelo antes de responder.",
                details = listOf(
                    "El modelo delibera internamente sobre tu pregunta",
                    "Genera respuestas más precisas y cuidadosas",
                    "Ideal para problemas complejos",
                ),
            ),
            TutorialStep(
                title = "Cuándo usarlo",
                description = "Activa el razonamiento cuando lo necesites.",
                details = listOf(
                    "Preguntas matemáticas o lógicas",
                    "Problemas que requieren múltiples pasos",
                    "Análisis profundo de temas complejos",
                    "Escritura de código o configuración",
                ),
            ),
            TutorialStep(
                title = "Impacto en el rendimiento",
                description = "El razonamiento profundo toma más tiempo.",
                details = listOf(
                    "Las respuestas tardarán más en llegar",
                    "Usar solo cuando lo necesites",
                    "Las respuestas serán más precisas",
                    "Puedes desactivarlo en cualquier momento",
                ),
            ),
            TutorialStep(
                title = "Cómo activarlo",
                description = "Es simple: usa el interruptor.",
                details = listOf(
                    "En la sección 'Agente', mueve el interruptor a ON",
                    "Presiona 'Entendido'",
                    "Ahora todas tus conversaciones usarán razonamiento",
                ),
            ),
        ),
        onDismiss = { showThinkingTutorial = false },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AresTabHeroHeader(
            title = "Configuración",
            subtitle = "CONTROL DEL SISTEMA Y MODELOS",
            tag = "SETUP",
        )

        // ── Model selection ────────────────────────────────────────────
        AresCard(title = "Modelo") {
            ModelPreference.entries.forEach { preference ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(preference.name, color = colors.onSurface, fontSize = 13.sp)
                            if (preference == ModelPreference.GWOPUS35) {
                                Text(
                                    "📚",
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .background(
                                            NeonRed.copy(alpha = 0.15f),
                                            RoundedCornerShape(4.dp),
                                        )
                                        .padding(2.dp),
                                )
                            }
                        }
                        Text(
                            when (preference) {
                                ModelPreference.E2B -> "~1.5 GB · más ligero"
                                ModelPreference.E4B -> "~3 GB · más capaz"
                                ModelPreference.GWOPUS35 -> "Qwen online · estilo Opus · requiere HF token"
                                else -> ""
                            },
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp,
                        )
                    }
                    RadioButton(
                        selected = state.modelPreference == preference,
                        onClick = {
                            viewModel.setModelPreference(preference)
                            if (preference == ModelPreference.GWOPUS35) {
                                showGwopusTutorial = true
                            }
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = NeonRed,
                            unselectedColor = colors.onSurfaceVariant,
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
                    Text("Razonamiento", color = colors.onSurface, fontSize = 13.sp)
                    Text("Más deliberación antes de responder", color = colors.onSurfaceVariant, fontSize = 11.sp)
                }
                Switch(
                    checked = state.thinkingEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.setThinkingEnabled(enabled)
                        if (enabled) {
                            showThinkingTutorial = true
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonRed,
                        checkedTrackColor = NeonRedDim,
                        uncheckedThumbColor = colors.onSurfaceVariant,
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
                    else -> colors.onSurfaceVariant
                },
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp,
            )
            SettingsActionButton(
                label = when {
                    state.modelPreference == ModelPreference.GWOPUS35 -> if (state.hfTokenConfigured) "Activar GWOPUS 3.5 online" else "Configurar token HF primero"
                    state.isInstalling -> "Instalando..."
                    else -> "Descargar / reinstalar"
                },
                enabled = !state.isInstalling && (state.modelPreference != ModelPreference.GWOPUS35 || state.hfTokenConfigured),
            ) { viewModel.installSelectedModel(context) }
            SettingsActionButton(
                label = if (state.modelPreference == ModelPreference.GWOPUS35) "Limpiar selección" else "Eliminar modelo local",
                enabled = state.installState is ModelInstallState.Ready,
            ) { showRemoveModelConfirmation = true }
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
                    color = if (state.geminiKeyConfigured) NeonRed else colors.onSurfaceVariant,
                    fontSize = 11.sp,
                )
                Text(
                    text = if (state.isOnline) "● Online" else "○ Offline",
                    color = if (state.isOnline) NeonRed else colors.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Text(
                "Con clave API usa Gemini Flash (gratis) para respuestas y análisis de imágenes. Sin ella solo funciona el modelo local.",
                color = colors.onSurfaceVariant,
                fontSize = 10.sp,
                lineHeight = 14.sp,
            )
            BasicTextField(
                value = geminiKeyInput,
                onValueChange = { geminiKeyInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background, RoundedCornerShape(10.dp))
                    .border(1.dp, colors.outline, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                textStyle = LocalTextStyle.current.copy(color = colors.onBackground, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                cursorBrush = SolidColor(colors.primary),
                singleLine = true,
                decorationBox = { inner ->
                    if (geminiKeyInput.isEmpty()) Text("AIza...", color = colors.onSurfaceVariant, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    inner()
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsActionButton("Guardar clave", modifier = Modifier.weight(1f)) {
                    viewModel.setGeminiApiKey(geminiKeyInput)
                    geminiKeyInput = ""
                    showGeminiTutorial = false
                }
                SettingsActionButton("Tutorial 📚", modifier = Modifier.weight(1f)) {
                    showGeminiTutorial = true
                }
            }
        }

        // ── HF token ──────────────────────────────────────────────────
        AresCard(title = "Token Hugging Face") {
            Text(
                if (state.hfTokenConfigured) "Token configurado ✓" else "Requerido para modelos privados",
                color = if (state.hfTokenConfigured) NeonRed else colors.onSurfaceVariant,
                fontSize = 11.sp,
            )
            BasicTextField(
                value = hfTokenInput,
                onValueChange = { hfTokenInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background, RoundedCornerShape(10.dp))
                    .border(1.dp, colors.outline, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                textStyle = LocalTextStyle.current.copy(color = colors.onBackground, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                cursorBrush = SolidColor(colors.primary),
                singleLine = true,
                decorationBox = { inner ->
                    if (hfTokenInput.isEmpty()) Text("hf_...", color = colors.onSurfaceVariant, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    inner()
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsActionButton("Guardar token", modifier = Modifier.weight(1f)) {
                    viewModel.setHfToken(hfTokenInput)
                    hfTokenInput = ""
                    showHfTutorial = false
                }
                SettingsActionButton("Tutorial 📚", modifier = Modifier.weight(1f)) {
                    showHfTutorial = true
                }
            }
        }

        // ── Reset ──────────────────────────────────────────────────────
        SettingsActionButton("Reabrir primer arranque") { viewModel.resetOnboarding() }
    }
}

@Composable
private fun AresCard(title: String, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant, RoundedCornerShape(14.dp))
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, color = colors.primary, fontSize = 10.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
        content()
    }
}

@Composable
private fun SettingsActionButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NeonRedDim,
            contentColor = Color.White,
            disabledContainerColor = NeonRedDim.copy(alpha = 0.35f),
            disabledContentColor = Color.White.copy(alpha = 0.65f),
        ),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
        )
    }
}

