package com.example.myapplication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.model.Message
import com.example.myapplication.model.MessageRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch



class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow<ChatUiState>(
        ChatUiState.Success(
            listOf(Message(role = MessageRole.AI, content = "你好！我是豆包AI，有什么可以帮你？"))
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // 模型名称使用官方示例中的（确保存在）
    private val MODEL_NAME = "doubao-seed-1-6-lite-251015"

    private var lastSentMessage: String? = null

    fun sendMessage(content: String) {
        if (content.trim().isEmpty()) return
        lastSentMessage = content

        viewModelScope.launch {
            val currentState = _uiState.value
            val currentMessages = when (currentState) {
                is ChatUiState.Success -> currentState.messages
                is ChatUiState.Loading -> currentState.messages
                is ChatUiState.Error -> currentState.messages
            }

            // 添加用户消息并进入加载状态
            val userMessage = Message(role = MessageRole.USER, content = content)
            val newMessages = currentMessages + userMessage
            _uiState.value = ChatUiState.Loading(newMessages)

            // 调用Repository（官方SDK）
            val result = repository.getAiResponse(
                model = MODEL_NAME,
                userMessage = content
            )

            if (result.isSuccess) {
                // 成功：添加AI回复
                val aiMessage = Message(
                    role = MessageRole.AI,
                    content = result.getOrThrow()
                )
                _uiState.value = ChatUiState.Success(newMessages + aiMessage)
            } else {
                // 失败：显示错误
                val errorMsg = result.exceptionOrNull()?.message ?: "调用失败"
                updateErrorState(newMessages, errorMsg)
                Log.e("ChatViewModel", "AI调用失败", result.exceptionOrNull())
            }
        }
    }

    fun retryLastMessage() {
        lastSentMessage?.let { sendMessage(it) }
    }

    private fun updateErrorState(currentMessages: List<Message>, errorMsg: String) {
        val errorMessage = Message(
            role = MessageRole.AI,
            content = "出错了：$errorMsg"
        )
        _uiState.value = ChatUiState.Error(
            messages = currentMessages + errorMessage,
            errorMessage = errorMsg
        )
    }

    // 页面销毁时关闭服务
    override fun onCleared() {
        super.onCleared()
        repository.shutdown()
    }
}