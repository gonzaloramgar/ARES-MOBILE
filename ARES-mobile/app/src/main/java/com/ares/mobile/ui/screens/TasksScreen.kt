package com.ares.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ares.mobile.ui.components.AresTabHeroHeader
import com.ares.mobile.ui.components.ConfirmationDialog
import com.ares.mobile.ui.theme.AresThemeMetallic
import com.ares.mobile.viewmodel.TasksViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun TasksScreen(viewModel: TasksViewModel) {
    AresThemeMetallic {
        TasksScreenContent(viewModel)
    }
}

@Composable
private fun TasksScreenContent(viewModel: TasksViewModel) {
    val colors = MaterialTheme.colorScheme
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    var taskToDelete by remember { mutableStateOf<Long?>(null) }
    var taskToDeleteTitle by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Confirmation dialog for deleting task
    ConfirmationDialog(
        isVisible = showDeleteConfirmation,
        title = "Eliminar tarea",
        message = "¿Eliminar la tarea '$taskToDeleteTitle'? No se puede deshacer.",
        confirmButtonText = "Eliminar",
        dismissButtonText = "Cancelar",
        isDestructive = true,
        onConfirm = {
            taskToDelete?.let { viewModel.deleteTask(it) }
            showDeleteConfirmation = false
            taskToDelete = null
            taskToDeleteTitle = ""
        },
        onDismiss = {
            showDeleteConfirmation = false
            taskToDelete = null
            taskToDeleteTitle = ""
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
            title = "Tareas",
            subtitle = "AUTOMATIZACIONES Y EVENTOS PROGRAMADOS",
            tag = "TASK",
        )

        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("⏱", fontSize = 32.sp)
                    Text("Sin tareas programadas", color = colors.onSurfaceVariant, fontSize = 13.sp)
                    Text("Dile a ARES que cree una alarma", color = colors.onSurfaceVariant, fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tasks, key = { it.id }) { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceVariant, RoundedCornerShape(12.dp))
                            .border(1.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                formatter.format(Date(task.triggerAtMillis)),
                                color = colors.primary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp,
                            )
                            Text(task.title, color = colors.onSurface, fontSize = 13.sp)
                            task.note?.takeIf { it.isNotBlank() }?.let {
                                Text(it, color = colors.onSurfaceVariant, fontSize = 11.sp)
                            }
                        }
                        IconButton(
                            onClick = {
                                taskToDelete = task.id
                                taskToDeleteTitle = task.title
                                showDeleteConfirmation = true
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
