package com.ares.mobile.ai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ares.mobile.AresApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ModelDownloadService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID = "ares_model_download"
        const val NOTIF_PROGRESS = 2001
        const val NOTIF_DONE = 2002
        const val ACTION_CANCEL = "com.ares.mobile.ACTION_CANCEL_DOWNLOAD"
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL) {
            scope.cancel()
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIF_PROGRESS, progressNotification("Iniciando descarga...", 0, indeterminate = true))

        val modelManager = (application as AresApplication).appContainer.modelManager

        scope.launch {
            try {
                modelManager.installPreferredModel().collect { state ->
                    when (state) {
                        is ModelInstallState.Downloading -> {
                            val percent = state.progressPercent
                            val text = "${state.variant.displayName}  ·  $percent%"
                            notify(NOTIF_PROGRESS, progressNotification(text, percent, indeterminate = percent == 0))
                        }
                        is ModelInstallState.Ready -> {
                            notify(NOTIF_PROGRESS, progressNotification("Listo · ${state.variant.displayName}", 100, indeterminate = false))
                            stopSelf()
                        }
                        is ModelInstallState.Error -> {
                            notify(NOTIF_DONE, errorNotification(state.message))
                            stopSelf()
                        }
                        else -> {}
                    }
                }
            } catch (_: Exception) {
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Notifications ──────────────────────────────────────────────────────

    private fun progressNotification(text: String, progress: Int, indeterminate: Boolean): Notification {
        val cancelPi = PendingIntent.getService(
            this, 0,
            Intent(this, ModelDownloadService::class.java).apply { action = ACTION_CANCEL },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("ARES · Descargando modelo")
            .setContentText(text)
            .setProgress(100, progress, indeterminate)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_delete, "Cancelar", cancelPi)
            .build()
    }

    private fun errorNotification(message: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("ARES · Error de descarga")
            .setContentText(message)
            .setAutoCancel(true)
            .build()

    private fun notify(id: Int, notification: Notification) {
        getSystemService(NotificationManager::class.java).notify(id, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Descarga modelo IA",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Progreso de descarga del modelo Gemma"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
