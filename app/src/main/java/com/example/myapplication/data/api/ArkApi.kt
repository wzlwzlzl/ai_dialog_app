package com.example.myapplication.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// 独立的消息类
data class ArkMessage(
    val role: String,
    val content: String
)

// 请求体
data class ArkRequest(
    val model: String,
    val messages: List<ArkMessage>
)

// 响应体
data class ArkResponse(
    val id: String,
    @SerializedName("object")
    val objectX: String,
    val created: Long,
    val model: String,
    val choices: List<ArkChoice>,
    val usage: ArkUsage
) {
    data class ArkChoice(
        val index: Int,
        val message: ArkMessage,
        val finish_reason: String
    )

    data class ArkUsage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )
}

// API 接口
interface ArkApiService {
    @POST("api/v3/chat/completions")
    suspend fun sendChatRequest(
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Authorization") auth: String,
        @Body request: ArkRequest
    ): Response<ArkResponse>
}