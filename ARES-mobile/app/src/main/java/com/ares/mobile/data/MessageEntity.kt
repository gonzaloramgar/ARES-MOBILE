package com.ares.mobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String = DEFAULT_CONVERSATION_ID,
    val role: String,
    val content: String,
    val timestamp: Long,
    val toolName: String? = null,
) {
    companion object {
        const val DEFAULT_CONVERSATION_ID = "default"
    }
}