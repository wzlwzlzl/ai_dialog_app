package com.example.myapplication.data.repository

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole
import com.volcengine.ark.runtime.service.ArkService
import com.example.myapplication.data.dao.ChatMessageDao
import com.example.myapplication.model.ChatMessageEntity
import com.example.myapplication.model.Message
import com.example.myapplication.model.toEntity
import com.example.myapplication.model.toMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ChatRepository(
    private val chatMessageDao: ChatMessageDao? = null // 设为可空并提供默认值
) {
    private val arkService by lazy {
        ArkService.builder()
            .apiKey("54d5a7cb-ffe2-4c77-b72f-90788cf7f795") // 替换为有效API密钥
            .baseUrl("https://ark.cn-beijing.volces.com/api/v3")
            .build()
    }

    // 获取历史消息：使用安全调用，为空时返回空流
    fun getHistoryMessages(conversationId: String = "default"): Flow<List<Message>> {
        return chatMessageDao?.getMessagesByConversationId(conversationId)
            ?.map { entities -> entities.map { it.toMessage() } }
            ?: emptyFlow() // 为空时返回空流
    }

    // 保存单条消息：使用安全调用，为空时不执行操作
    suspend fun saveMessage(message: Message, conversationId: String = "default") {
        withContext(Dispatchers.IO) {
            chatMessageDao?.insertMessage(message.toEntity(conversationId))
        }
    }

    // 保存多条消息：使用安全调用，为空时不执行操作
    suspend fun saveMessages(messages: List<Message>, conversationId: String = "default") {
        withContext(Dispatchers.IO) {
            chatMessageDao?.insertMessages(messages.map { it.toEntity(conversationId) })
        }
    }

    // 清除指定对话：使用安全调用，为空时不执行操作
    suspend fun clearConversation(conversationId: String = "default") {
        withContext(Dispatchers.IO) {
            chatMessageDao?.deleteMessagesByConversationId(conversationId)
        }
    }

    // 清除所有消息：使用安全调用，为空时不执行操作
    suspend fun clearAllMessages() {
        withContext(Dispatchers.IO) {
            chatMessageDao?.deleteAllMessages()
        }
    }

    // AI响应获取（已有代码）
    suspend fun getAiResponse(model: String, userMessage: String): Result<String> {
        var result: Result<String> = Result.failure(Exception("初始化错误"))

        withContext(Dispatchers.IO) {
            try {
                val messages = listOf(
                    ChatMessage.builder()
                        .role(ChatMessageRole.USER)
                        .content(userMessage)
                        .build()
                )

                val request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .build()

                val response = arkService.createChatCompletion(request)
                val aiContent: String = response.choices.firstOrNull()?.message?.content?.toString()
                    ?: "未获取到回复"

                result = Result.success(aiContent)
            } catch (e: Exception) {
                result = Result.failure(e)
            }
        }

        return result
    }

    fun shutdown() {
        arkService.shutdownExecutor()
    }
}