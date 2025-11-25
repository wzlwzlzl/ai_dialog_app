package com.example.myapplication.data.repository

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole
import com.volcengine.ark.runtime.service.ArkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository {
    private val arkService by lazy {
        ArkService.builder()
            .apiKey("54d5a7cb-ffe2-4c77-b72f-90788cf7f795") // 替换为你的有效API密钥
            .baseUrl("https://ark.cn-beijing.volces.com/api/v3")
            .build()
    }

    // 明确返回类型为Result<String>，不依赖编译器推断
    suspend fun getAiResponse(model: String, userMessage: String): Result<String> {
        // 1. 定义一个临时变量，明确类型为Result<String>
        var result: Result<String> = Result.failure(Exception("初始化错误"))

        // 2. 在IO线程执行网络请求，不直接返回withContext的结果
        withContext(Dispatchers.IO) {
            try {
                // 构建消息
                val messages = listOf(
                    ChatMessage.builder()
                        .role(ChatMessageRole.USER)
                        .content(userMessage)
                        .build()
                )

                // 构建请求
                val request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .build()

                // 调用SDK
                val response = arkService.createChatCompletion(request)

                // 解析结果（强制转换为String，确保类型）
                val aiContent: String = response.choices.firstOrNull()?.message?.content?.toString()
                    ?: "未获取到回复"

                // 赋值成功结果（明确类型）
                result = Result.success(aiContent)
            } catch (e: Exception) {
                // 赋值失败结果（明确类型）
                result = Result.failure(e)
            }
        }

        // 3. 直接返回明确类型的result变量
        return result
    }

    fun shutdown() {
        arkService.shutdownExecutor()
    }
}