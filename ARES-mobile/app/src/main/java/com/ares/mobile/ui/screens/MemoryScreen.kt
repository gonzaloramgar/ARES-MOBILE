package com.ares.mobile.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ares.mobile.ui.components.AresTabHeroHeader
import com.ares.mobile.ui.components.ConfirmationDialog
import com.ares.mobile.ui.theme.AresThemeMetallic
import com.ares.mobile.viewmodel.MemoryViewModel

@Composable
fun MemoryScreen(viewModel: MemoryViewModel) {
    AresThemeMetallic {
        MemoryScreenContent(viewModel)
    }
}

@Composable
private fun MemoryScreenContent(viewModel: MemoryViewModel) {
    val colors = MaterialTheme.colorScheme
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var memoryToDelete by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Confirmation dialog for deleting memory
    ConfirmationDialog(
        isVisible = showDeleteConfirmation,
        title = "Eliminar recuerdo",
        message = "¿Eliminar el recuerdo '$memoryToDelete'? No se puede deshacer.",
        confirmButtonText = "Eliminar",
        dismissButtonText = "Cancelar",
        isDestructive = true,
        onConfirm = {
            memoryToDelete?.let { viewModel.deleteMemory(it) }
            showDeleteConfirmation = false
            memoryToDelete = null
        },
        onDismiss = {
            showDeleteConfirmation = false
            memoryToDelete = null
        },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AresTabHeroHeader(
            title = "Memoria",
            subtitle = "HECHOS CLAVE Y CONTEXTO PERSISTENTE",
            tag = "MEM",
        )

        // ── Add memory form ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(14.dp))
                .border(1.dp, colors.outline, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Añadir recuerdo", color = colors.primary, fontSize = 10.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)

            AresInputField(
                value = key,
                placeholder = "Clave  (ej: nombre)",
                colors = colors,
                onValueChange = { key = it },
            )
            AresInputField(
                value = value,
                placeholder = "Valor  (ej: Carlos)",
                colors = colors,
                onValueChange = { value = it },
            )

            AresFilledButton(
                label = "Guardar",
                enabled = key.isNotBlank() && value.isNotBlank(),
            ) {
                viewModel.saveMemory(key, value)
                key = ""
                value = ""
            }
        }

        // ── Memory list ────────────────────────────────────────────────
        if (memories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text("Sin recuerdos guardados", color = colors.onSurfaceVariant, fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(memories, key = { it.key }) { memory ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceVariant, RoundedCornerShape(10.dp))
                            .border(1.dp, colors.outlineVariant, RoundedCornerShape(10.dp))
                            .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                memory.key,
                                color = colors.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp,
                            )
                            Text(memory.value, color = colors.onSurface, fontSize = 13.sp)
                        }
                        IconButton(
                            onClick = {
                                memoryToDelete = memory.key
                                showDeleteConfirmation = true
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun AresInputField(
    value: String,
    placeholder: String,
    colors: androidx.compose.material3.ColorScheme,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background, RoundedCornerShape(10.dp))
            .border(1.dp, colors.outline, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        textStyle = LocalTextStyle.current.copy(color = colors.onBackground, fontSize = 13.sp),
        cursorBrush = SolidColor(colors.primary),
        singleLine = true,
        decorationBox = { inner ->
            if (value.isEmpty()) Text(placeholder, color = colors.onSurfaceVariant, fontSize = 13.sp)
            inner()
        },
    )
}
