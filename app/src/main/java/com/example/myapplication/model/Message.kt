package com.example.myapplication.model

// 消息角色枚举（新手推荐用枚举，避免字符串硬编码）
enum class MessageRole {
    USER,   // 用户发送的消息
    AI      // AI 回复的消息
}

// 消息数据类（MVI 中的 "State 数据"）
data class Message(
    val id: String = System.currentTimeMillis().toString(),  // 唯一ID（用时间戳简单生成）
    val role: MessageRole,  // 消息角色
    val content: String,    // 消息内容
    val time: Long = System.currentTimeMillis()  // 消息时间戳（后续可用于排序）
)