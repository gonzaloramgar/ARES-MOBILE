package com.ares.mobile.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ares.mobile.agent.ChatMessage
import com.ares.mobile.agent.MessageRole
import com.ares.mobile.ui.components.ConfirmationDialog
import com.ares.mobile.ui.components.MessageBubble
import com.ares.mobile.ui.components.QuickActionsBar
import com.ares.mobile.ui.components.TypingIndicator
import com.ares.mobile.ui.theme.BackgroundDeep
import com.ares.mobile.ui.theme.BorderSubtle
import com.ares.mobile.ui.theme.NeonRed
import com.ares.mobile.ui.theme.NeonRedBorder
import com.ares.mobile.ui.theme.NeonRedDim
import com.ares.mobile.ui.theme.SurfaceDark
import com.ares.mobile.ui.theme.TextMuted
import com.ares.mobile.ui.theme.TextPrimary
import com.ares.mobile.ui.theme.TextSecondary
import com.ares.mobile.ui.theme.AresThemeVibrant
import com.ares.mobile.viewmodel.ChatViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    AresThemeVibrant {
        ChatScreenContent(viewModel)
    }
}

@Composable
private fun ChatScreenContent(viewModel: ChatViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var showClearConfirmation by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            saveLatestCapture(context, bitmap)
            viewModel.submitMessage("/camera")
        }
    }
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val transcript = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
        if (result.resultCode == Activity.RESULT_OK && transcript.isNotBlank()) {
            viewModel.submitMessage(transcript)
        }
    }

    LaunchedEffect(state.messages.size, state.draftResponse) {
        val count = state.messages.size + if (state.draftResponse.isNotBlank() || state.isGenerating) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    // Confirmation dialog for clearing conversation
    ConfirmationDialog(
        isVisible = showClearConfirmation,
        title = "Borrar conversación",
        message = "¿Estás seguro? Se eliminarán todos los mensajes. Esta acción no se puede deshacer.",
        confirmButtonText = "Eliminar",
        dismissButtonText = "Cancelar",
        isDestructive = true,
        onConfirm = {
            viewModel.clearConversation()
            showClearConfirmation = false
        },
        onDismiss = { showClearConfirmation = false },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep),
    ) {
        // ── Custom header ──────────────────────────────────────────────
        AresChatHeader(
            modelHeadline = state.modelHeadline,
            isGenerating = state.isGenerating,
            onClear = { showClearConfirmation = true },
        )

        // ── Install hint ───────────────────────────────────────────────
        if (state.installHint.isNotBlank()) {
            Text(
                text = state.installHint,
                color = NeonRed.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonRed.copy(alpha = 0.05f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }

        // ── Messages ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (state.messages.isEmpty() && !state.isGenerating) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("⬡", fontSize = 44.sp, color = NeonRed.copy(alpha = 0.4f))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "ARES MOBILE",
                        color = NeonRed.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp,
                    )
                    Text(
                        state.modelHeadline,
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                items(state.messages, key = { it.id.takeIf { id -> id != 0L } ?: it.timestamp }) { message ->
                    MessageBubble(message)
                }
            if (state.draftResponse.isNotBlank()) {
                item {
                    MessageBubble(
                        ChatMessage(
                            role = MessageRole.Assistant,
                            content = state.draftResponse,
                            timestamp = System.currentTimeMillis(),
                            toolName = state.activeTool,
                        ),
                    )
                }
            }
            if (state.isGenerating && state.draftResponse.isBlank()) {
                item {
                    TypingIndicator(modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }
            }
        }
        } // end Box

        // ── Quick actions ──────────────────────────────────────────────
        QuickActionsBar(
            actions = state.quickActions,
            onActionClick = { action -> viewModel.runQuickAction(action.prompt) },
        )

        // ── Input area ─────────────────────────────────────────────────
        AresChatInput(
            input = state.input,
            isGenerating = state.isGenerating,
            onInputChange = viewModel::onInputChanged,
            onSend = viewModel::sendMessage,
            onCamera = { cameraLauncher.launch(null) },
            onMic = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla con ARES")
                }
                speechLauncher.launch(intent)
            },
        )
    }
}

@Composable
private fun AresChatHeader(
    modelHeadline: String,
    isGenerating: Boolean,
    onClear: () -> Unit,
) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "dot",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDeep)
            .border(bottom = 1.dp, color = NeonRedBorder)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Logo
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AresDiamondLogo()
            Text(
                text = "ARES",
                color = NeonRed,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
            )
        }

        // Model indicator + clear button
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            NeonRed.copy(alpha = if (isGenerating) dotAlpha else 1f),
                            CircleShape,
                        ),
                )
                Text(
                    text = modelHeadline,
                    color = NeonRed.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
            IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = "Borrar conversación",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun AresDiamondLogo() {
    val transition = rememberInfiniteTransition(label = "miniLogo")
    val diamondRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Restart),
        label = "diamondRotation",
    )
    val glowPulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "glowPulse",
    )

    Box(
        modifier = Modifier
            .size(34.dp)
            .background(Brush.linearGradient(listOf(NeonRed, NeonRedDim)), RoundedCornerShape(10.dp))
            .border(1.dp, NeonRed.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(26.dp)) {
            val c = center
            val r = size.minDimension / 2f

            drawCircle(
                color = Color.Black.copy(alpha = 0.18f),
                radius = r,
            )
            drawCircle(
                color = NeonRed.copy(alpha = 0.18f * glowPulse),
                radius = r * 0.9f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx()),
            )

            rotate(degrees = diamondRotation, pivot = c) {
                val diamond = Path().apply {
                    moveTo(c.x, c.y - r * 0.62f)
                    lineTo(c.x + r * 0.62f, c.y)
                    lineTo(c.x, c.y + r * 0.62f)
                    lineTo(c.x - r * 0.62f, c.y)
                    close()
                }
                drawPath(
                    path = diamond,
                    brush = Brush.radialGradient(
                        listOf(NeonRed.copy(alpha = 0.95f), NeonRedDim.copy(alpha = 0.85f)),
                        center = c,
                        radius = r,
                    ),
                )
                drawPath(
                    path = diamond,
                    color = Color.White.copy(alpha = 0.35f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                )
            }

            drawCircle(color = Color.White.copy(alpha = 0.75f), radius = 1.4.dp.toPx(), center = c)
        }
    }
}

@Composable
private fun AresChatInput(
    input: String,
    isGenerating: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onCamera: () -> Unit,
    onMic: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDeep)
            .border(top = 1.dp, color = NeonRedBorder)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Text field
        BasicTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(22.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                        if (event.isShiftPressed) {
                            onInputChange(input + "\n"); true
                        } else {
                            onSend(); true
                        }
                    } else false
                },
            textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 13.sp, lineHeight = 19.sp),
            cursorBrush = SolidColor(NeonRed),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
            ),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            decorationBox = { inner ->
                if (input.isEmpty()) Text("Escribe o habla...", color = TextSecondary, fontSize = 13.sp)
                inner()
            },
        )

        // Action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Camera
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(1.dp, NeonRedBorder, CircleShape)
                    .clickable { onCamera() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Cámara", tint = NeonRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))

            // Mic
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(1.dp, NeonRedBorder, CircleShape)
                    .clickable { onMic() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voz", tint = NeonRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.weight(1f))

            // Send button
            val canSend = !isGenerating && input.isNotBlank()
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        if (canSend) Brush.linearGradient(listOf(NeonRed, NeonRedDim))
                        else Brush.linearGradient(listOf(TextMuted, TextMuted)),
                        CircleShape,
                    )
                    .clip(CircleShape)
                    .clickable(enabled = canSend) { onSend() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = if (canSend) "Enviar" else "Escribir mensaje para enviar",
                    tint = if (canSend) androidx.compose.ui.graphics.Color.White else TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// Extension for single-side border
private fun Modifier.border(bottom: Dp? = null, top: Dp? = null, color: androidx.compose.ui.graphics.Color): Modifier {
    val dp = bottom ?: top ?: return this
    return then(drawBehind {
        val y = if (bottom != null) size.height else 0f
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = dp.toPx(),
        )
    })
}

private fun saveLatestCapture(context: Context, bitmap: Bitmap) {
    val dir = File(context.filesDir, "captures").also { it.mkdirs() }
    FileOutputStream(File(dir, "latest.jpg")).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
}
