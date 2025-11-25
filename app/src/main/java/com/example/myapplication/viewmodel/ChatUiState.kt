// app/src/main/java/com/example/myapplication/viewmodel/ChatUiState.kt
package com.example.myapplication.viewmodel

import com.example.myapplication.model.Message

sealed class ChatUiState {
    // 成功状态：包含消息列表
    data class Success(val messages: List<Message>) : ChatUiState()
    // 加载状态：包含当前已有的消息列表
    data class Loading(val messages: List<Message>) : ChatUiState()
    // 错误状态：包含当前消息列表和错误信息
    data class Error(val messages: List<Message>, val errorMessage: String) : ChatUiState()
}