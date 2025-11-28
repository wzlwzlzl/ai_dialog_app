package com.example.myapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "USER" 或 "AI"
    val content: String,
    val timestamp: Long,
    val conversationId: String = "default"
)

// 添加转换扩展函数（关键部分）
fun ChatMessageEntity.toMessage(): Message {
    return Message(
        id = id.toString(),
        role = if (role == "USER") MessageRole.USER else MessageRole.AI,
        content = content,
        time = timestamp
    )
}

fun Message.toEntity(conversationId: String = "default"): ChatMessageEntity {
    return ChatMessageEntity(
        id = if (this.id.isEmpty()) 0 else this.id.toLongOrNull() ?: 0,
        role = this.role.name,
        content = this.content,
        timestamp = this.time,
        conversationId = conversationId
    )
}