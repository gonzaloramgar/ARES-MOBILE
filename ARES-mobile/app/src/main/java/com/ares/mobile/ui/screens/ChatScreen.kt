package com.ares.mobile.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ares.mobile.agent.ChatMessage
import com.ares.mobile.agent.MessageRole
import com.ares.mobile.ui.components.MessageBubble
import com.ares.mobile.ui.components.QuickActionsBar
import com.ares.mobile.ui.components.TypingIndicator
import com.ares.mobile.viewmodel.ChatViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) {
            saveLatestCapture(context, bitmap)
            viewModel.submitMessage("/camera")
        }
    }
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val transcript = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
        if (result.resultCode == Activity.RESULT_OK && transcript.isNotBlank()) {
            viewModel.submitMessage(transcript)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text("ARES", style = MaterialTheme.typography.titleMedium)
                        Text(state.modelHeadline, style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::clearConversation) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Borrar conversación")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
        ) {
            if (state.installHint.isNotBlank()) {
                Text(
                    text = state.installHint,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
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
                            )
                        )
                    }
                }
                if (state.isGenerating) {
                    item {
                        TypingIndicator(modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                    }
                }
            }

            QuickActionsBar(
                actions = state.quickActions,
                onActionClick = { action -> viewModel.runQuickAction(action.prompt) },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    tonalElevation = 6.dp,
                    shadowElevation = 10.dp,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = state.input,
                            onValueChange = viewModel::onInputChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onPreviewKeyEvent { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Enter) {
                                        if (keyEvent.isShiftPressed) {
                                            viewModel.onInputChanged(state.input + "\n")
                                        } else {
                                            viewModel.sendMessage()
                                        }
                                        true
                                    } else {
                                        false
                                    }
                                },
                            placeholder = { Text("Escribe tu mensaje") },
                            minLines = 1,
                            maxLines = 6,
                            shape = RoundedCornerShape(20.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Send,
                            ),
                            keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                            ),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = { cameraLauncher.launch(null) }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Cámara")
                            }
                            IconButton(
                                onClick = {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla con ARES")
                                    }
                                    speechLauncher.launch(intent)
                                }
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Voz")
                            }
                            IconButton(onClick = { viewModel.onInputChanged(state.input + "\n") }) {
                                Text("↵")
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            FilledIconButton(
                                onClick = viewModel::sendMessage,
                                modifier = Modifier.size(42.dp),
                            ) {
                                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun saveLatestCapture(context: Context, bitmap: Bitmap) {
    val capturesDir = File(context.filesDir, "captures")
    if (!capturesDir.exists()) {
        capturesDir.mkdirs()
    }
    val captureFile = File(capturesDir, "latest.jpg")
    FileOutputStream(captureFile).use { output ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
    }
}