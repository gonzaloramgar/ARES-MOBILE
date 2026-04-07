package com.ares.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_tasks")
data class ScheduledTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String? = null,
    val triggerAtMillis: Long,
    val isEnabled: Boolean = true,
)