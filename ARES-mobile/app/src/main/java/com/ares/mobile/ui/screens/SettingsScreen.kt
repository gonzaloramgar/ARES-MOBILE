package com.ares.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ares.mobile.ai.ModelInstallState
import com.ares.mobile.ai.ModelPreference
import com.ares.mobile.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var hfTokenInput by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Ajustes", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Modelo", style = MaterialTheme.typography.titleSmall)
                ModelPreference.entries.forEach { preference ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(preference.name)
                        RadioButton(
                            selected = state.modelPreference == preference,
                            onClick = { viewModel.setModelPreference(preference) },
                        )
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Thinking mode", style = MaterialTheme.typography.titleSmall)
                    Text("Añade más deliberación al agente antes de responder", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = state.thinkingEnabled, onCheckedChange = viewModel::setThinkingEnabled)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Instalación del modelo", style = MaterialTheme.typography.titleSmall)
                if (state.installState is ModelInstallState.Downloading) {
                    LinearProgressIndicator(
                        progress = { (state.installState as ModelInstallState.Downloading).progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(
                    when (val installState = state.installState) {
                        is ModelInstallState.Missing -> "Pendiente: ${installState.expectedPath}"
                        is ModelInstallState.Ready -> "Listo en ${installState.path}"
                        is ModelInstallState.Downloading -> "Descargando ${installState.variant.displayName}: ${installState.progressPercent}%"
                        is ModelInstallState.Error -> installState.message
                        ModelInstallState.Checking -> "Verificando"
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(
                    onClick = viewModel::installSelectedModel,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isInstalling,
                ) {
                    Text(if (state.isInstalling) "Instalando..." else "Descargar o reinstalar")
                }
                Button(
                    onClick = viewModel::removeInstalledModel,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.installState is ModelInstallState.Ready,
                ) {
                    Text("Eliminar modelo local")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Token Hugging Face", style = MaterialTheme.typography.titleSmall)
                Text(
                    if (state.hfTokenConfigured) "Token configurado" else "Necesario para modelos con 401/403",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = hfTokenInput,
                    onValueChange = { hfTokenInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("hf_...") },
                    singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            viewModel.setHfToken(hfTokenInput)
                            hfTokenInput = ""
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Guardar token")
                    }
                    Button(
                        onClick = {
                            viewModel.setHfToken("")
                            hfTokenInput = ""
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Quitar token")
                    }
                }
            }
        }

        Button(onClick = viewModel::resetOnboarding, modifier = Modifier.fillMaxWidth()) {
            Text("Reabrir primer arranque")
        }
    }
}