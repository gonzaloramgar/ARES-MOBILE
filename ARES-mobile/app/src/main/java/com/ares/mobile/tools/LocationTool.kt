package com.ares.mobile.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.ares.mobile.agent.ITool
import com.ares.mobile.agent.ToolDefinition
import com.ares.mobile.agent.ToolResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.resume

class LocationTool(
    private val context: Context,
) : ITool {
    override val definition: ToolDefinition = ToolDefinition(
        name = "location",
        description = "Obtiene la ubicación actual del dispositivo.",
        parameters = buildJsonObject {
            put("action", "current")
        },
    )

    override suspend fun execute(arguments: JsonObject): ToolResult {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            return ToolResult(false, "Permiso de ubicación no concedido")
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val location = suspendCancellableCoroutine { continuation ->
            fusedClient.lastLocation
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
        }

        return if (location == null) {
            ToolResult(false, "No se pudo obtener la ubicación actual")
        } else {
            ToolResult(
                success = true,
                content = "Lat ${location.latitude}, Lon ${location.longitude}",
                data = buildJsonObject {
                    put("latitude", location.latitude)
                    put("longitude", location.longitude)
                },
            )
        }
    }
}