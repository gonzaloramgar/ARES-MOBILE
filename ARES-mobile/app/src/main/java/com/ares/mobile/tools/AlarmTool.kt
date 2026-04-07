package com.ares.mobile.tools

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ares.mobile.agent.ITool
import com.ares.mobile.agent.ToolDefinition
import com.ares.mobile.agent.ToolResult
import com.ares.mobile.data.ScheduledTaskDao
import com.ares.mobile.data.ScheduledTaskEntity
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class AlarmTool(
    private val context: Context,
    private val scheduledTaskDao: ScheduledTaskDao,
) : ITool {
    override val definition: ToolDefinition = ToolDefinition(
        name = "alarm",
        description = "Programa un recordatorio local mediante AlarmManager.",
        parameters = buildJsonObject {
            put("minutesFromNow", "number")
            put("title", "string")
            put("note", "string?")
        },
    )

    override suspend fun execute(arguments: JsonObject): ToolResult {
        val minutes = arguments["minutesFromNow"]?.jsonPrimitive?.content?.toLongOrNull() ?: 10L
        val title = arguments["title"]?.jsonPrimitive?.content ?: "Recordatorio ARES"
        val note = arguments["note"]?.jsonPrimitive?.content
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L

        val taskId = scheduledTaskDao.upsert(
            ScheduledTaskEntity(
                title = title,
                note = note,
                triggerAtMillis = triggerAt,
            )
        )

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.ares.mobile.ALARM_TRIGGER"
            putExtra("title", title)
            putExtra("note", note)
            putExtra("taskId", taskId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }

        return ToolResult(true, "Alarma programada para dentro de $minutes minuto(s)")
    }
}