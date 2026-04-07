package com.ares.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long,
)