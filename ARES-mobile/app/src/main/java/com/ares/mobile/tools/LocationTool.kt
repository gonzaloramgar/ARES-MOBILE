package com.ares.mobile.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.ares.mobile.agent.ITool
import com.ares.mobile.agent.ToolDefinition
import com.ares.mobile.agent.ToolResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Locale
import kotlin.coroutines.resume

class LocationTool(
    private val context: Context,
) : ITool {
    override val definition: ToolDefinition = ToolDefinition(
        name = "location",
        description = "Obtiene la ubicación actual del dispositivo con dirección legible.",
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
            return ToolResult(false, "Permiso de ubicación no concedido. Ve a Ajustes → Permisos → Ubicación para activarlo.")
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val location = suspendCancellableCoroutine { continuation ->
            fusedClient.lastLocation
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
        }

        if (location == null) {
            return ToolResult(false, "No se pudo obtener la ubicación. Asegúrate de tener el GPS activado y haber usado la ubicación recientemente.")
        }

        val lat = location.latitude
        val lon = location.longitude
        val address = withContext(Dispatchers.IO) { reverseGeocode(lat, lon) }

        return ToolResult(
            success = true,
            content = address,
            data = buildJsonObject {
                put("latitude", lat)
                put("longitude", lon)
                put("address", address)
            },
        )
    }

    @Suppress("DEPRECATION")
    private fun reverseGeocode(lat: Double, lon: Double): String {
        if (!Geocoder.isPresent()) return coordsFallback(lat, lon)
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val list = geocoder.getFromLocation(lat, lon, 1)
            val addr = list?.firstOrNull() ?: return coordsFallback(lat, lon)

            buildString {
                val street = listOfNotNull(addr.thoroughfare, addr.subThoroughfare)
                    .joinToString(" ").trim()
                val district = addr.subLocality?.trim()
                val city = addr.locality?.trim()
                val region = addr.adminArea?.trim()
                val country = addr.countryName?.trim()

                val parts = listOfNotNull(
                    street.takeIf { it.isNotBlank() },
                    district?.takeIf { it.isNotBlank() && it != city },
                    city?.takeIf { it.isNotBlank() },
                    region?.takeIf { it.isNotBlank() && it != city },
                    country?.takeIf { it.isNotBlank() },
                )
                append(parts.joinToString(", "))
            }.takeIf { it.isNotBlank() } ?: coordsFallback(lat, lon)
        } catch (_: Exception) {
            coordsFallback(lat, lon)
        }
    }

    private fun coordsFallback(lat: Double, lon: Double) =
        "%.5f° N, %.5f° E".format(lat, lon)
}
