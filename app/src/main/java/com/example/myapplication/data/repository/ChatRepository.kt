package com.example.myapplication.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import com.example.myapplication.data.dao.ChatMessageDao
import com.example.myapplication.model.ConversationSummary
import com.example.myapplication.model.Message
import com.example.myapplication.model.MessageRole
import com.example.myapplication.model.toEntity
import com.example.myapplication.model.toMessage
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole
import com.volcengine.ark.runtime.model.images.generation.GenerateImagesRequest
import com.volcengine.ark.runtime.service.ArkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class ChatRepository(
    private val chatMessageDao: ChatMessageDao? = null // 设为可空并提供默认值
) {
    private val arkService by lazy {
        ArkService.builder()
            .apiKey("54d5a7cb-ffe2-4c77-b72f-90788cf7f795") // my api key
            .baseUrl("https://ark.cn-beijing.volces.com/api/v3")
            .build()
    }

    private val httpClient by lazy { OkHttpClient() }

    fun getConversationSummaries(): Flow<List<ConversationSummary>> {
        return chatMessageDao?.getConversationSummaries() ?: emptyFlow()
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

    // AI 文本响应获取
    suspend fun getAiResponse(
        model: String,
        userMessage: String,
        conversationId: String = "default"
    ): Result<String> {
        var result: Result<String> = Result.failure(Exception("初始化错误"))

        withContext(Dispatchers.IO) {
            try {
                // 1. 获取最近的10条历史消息
                val history = getHistoryMessages(conversationId).first().takeLast(10)

                // 2. 将历史消息转换为API需要的格式
                val historyMessages = history.map {
                    ChatMessage.builder()
                        .role(if (it.role == MessageRole.USER) ChatMessageRole.USER else ChatMessageRole.ASSISTANT)
                        .content(it.content)
                        .build()
                }

                // 3. 创建当前用户消息
                val currentUserMessage = ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .content(userMessage)
                    .build()

                // 4. 组合历史消息和当前消息
                val messages = historyMessages + currentUserMessage

                val request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .build()

                val response = arkService.createChatCompletion(request)
                val aiContent: String = response.choices.firstOrNull()?.message?.content?.toString()
                    ?: "未获取到回复"

                result = Result.success(aiContent)
            } catch (e: Exception) {
                result = Result.failure(Exception("获取AI回复失败"))
            }
        }

        return result
    }

    // 文生图：生成图片并尝试保存到本地，返回本地 Uri 或图片 URL
    suspend fun generateImage(
        model: String,
        prompt: String,
        context: Context? = null
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val request = GenerateImagesRequest.builder()
                    .model(model)
                    .prompt(prompt)
                    .build()

                val response = arkService.generateImages(request)
                val url = response.data.firstOrNull()?.url
                    ?: return@withContext Result.failure(Exception("未获取到图片地址"))

                // 如果没有上下文，只返回图片 URL
                if (context == null) {
                    return@withContext Result.success(url)
                }

                // 下载图片数据
                val httpRequest = Request.Builder().url(url).build()
                val httpResponse = httpClient.newCall(httpRequest).execute()
                if (!httpResponse.isSuccessful) {
                    return@withContext Result.failure(Exception("下载图片失败"))
                }
                val bytes = httpResponse.body?.bytes()
                    ?: return@withContext Result.failure(Exception("图片内容为空"))

                // 通过 MediaStore 保存到系统相册
                val resolver = context.contentResolver
                val fileName = "doubao_${System.currentTimeMillis()}.png"
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/Doubao"
                    )
                }

                val uri = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return@withContext Result.failure(Exception("无法创建图片文件"))

                resolver.openOutputStream(uri)?.use { output ->
                    output.write(bytes)
                } ?: return@withContext Result.failure(Exception("无法写入图片文件"))

                Result.success(uri.toString())
            } catch (e: Exception) {
                Result.failure(Exception("生成图片失败"))
            }
        }
    }

    // 使用指定模型对文本进行概括，生成更短的标题
    suspend fun summarizeText(
        model: String,
        originalText: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val systemMessage = ChatMessage.builder()
                    .role(ChatMessageRole.SYSTEM)
                    .content("请将用户提供的内容概括为不超过16个字的标题。只输出标题本身。")
                    .build()

                val userMessage = ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .content(originalText)
                    .build()

                val request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(listOf(systemMessage, userMessage))
                    .build()

                val response = arkService.createChatCompletion(request)
                val summary = response.choices.firstOrNull()?.message?.content?.toString()
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: return@withContext Result.failure(Exception("概括失败"))

                Result.success(summary)
            } catch (e: Exception) {
                Result.failure(Exception("概括失败"))
            }
        }
    }

    fun shutdown() {
        arkService.shutdownExecutor()
    }
}