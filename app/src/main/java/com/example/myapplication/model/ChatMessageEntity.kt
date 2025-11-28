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

// 优化 toEntity() 中的 id 处理
fun Message.toEntity(conversationId: String = "default"): ChatMessageEntity {
    return ChatMessageEntity(
        id = this.id.toLongOrNull() ?: 0, // 直接转换，空字符串会转为 0（配合 autoGenerate = true）
        role = this.role.name, // 确保存储的是枚举名称（USER/AI）
        content = this.content,
        timestamp = this.time,
        conversationId = conversationId
    )
}