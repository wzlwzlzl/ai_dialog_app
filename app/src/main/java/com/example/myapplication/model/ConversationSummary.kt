package com.example.myapplication.model

/**
 * 轻量级的对话摘要，列表中展示历史对话。
 */
data class ConversationSummary(
    val conversationId: String,
    val title: String?,
    val lastUpdated: Long
)

