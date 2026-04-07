package com.ares.mobile.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ares.mobile.ai.ModelInstallState
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("ARES", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Configuración inicial para permisos, modelo local y preferencias del agente.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(20.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Estado del modelo", style = MaterialTheme.typography.titleSmall)
                if (state.installState is ModelInstallState.Downloading) {
                    LinearProgressIndicator(
                        progress = { (state.installState as ModelInstallState.Downloading).progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Text(
                    when (val installState = state.installState) {
                        is ModelInstallState.Missing -> "Falta instalar ${installState.variant.displayName}. Ruta esperada: ${installState.expectedPath}"
                        is ModelInstallState.Ready -> "Modelo listo en ${installState.path}"
                        is ModelInstallState.Downloading -> "Descargando ${installState.variant.displayName}: ${installState.progressPercent}%"
                        is ModelInstallState.Error -> installState.message
                        ModelInstallState.Checking -> "Comprobando instalación del modelo"
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = { permissionLauncher.launch(requestedPermissions) }, modifier = Modifier.fillMaxWidth()) {
            Text("Solicitar permisos")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = viewModel::installSelectedModel,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isInstalling,
        ) {
            Text(if (state.isInstalling) "Instalando modelo..." else "Descargar / verificar modelo")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                viewModel.markFirstRunCompleted()
                onContinue()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.installState is ModelInstallState.Ready) "Continuar a la app" else "Continuar sin modelo")
        }
    }
}