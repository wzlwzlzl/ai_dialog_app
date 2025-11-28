package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.model.ChatMessageEntity
import com.example.myapplication.model.ConversationSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    // 插入单条消息
    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)

    // 插入多条消息
    @Insert
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    // 获取指定对话的所有消息，按时间排序
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesByConversationId(conversationId: String = "default"): Flow<List<ChatMessageEntity>>

    // 删除指定对话的所有消息
    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesByConversationId(conversationId: String = "default")

    // 删除所有消息
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    // 查询所有对话的摘要信息（首条用户消息作为标题，按最新时间倒序）
    @Query(
        """
        SELECT 
            cm.conversationId AS conversationId,
            (
                SELECT content FROM chat_messages 
                WHERE conversationId = cm.conversationId 
                AND role = 'USER'
                ORDER BY timestamp ASC 
                LIMIT 1
            ) AS title,
            MAX(cm.timestamp) AS lastUpdated
        FROM chat_messages AS cm
        GROUP BY cm.conversationId
        ORDER BY lastUpdated DESC
        """
    )
    fun getConversationSummaries(): Flow<List<ConversationSummary>>
}