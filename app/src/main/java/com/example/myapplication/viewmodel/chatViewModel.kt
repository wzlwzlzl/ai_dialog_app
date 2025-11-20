package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Message
import com.example.myapplication.model.MessageRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    // MVI 核心：用 StateFlow 管理界面状态（消息列表）
    // _uiState 是私有可变的，仅 ViewModel 内部可修改
    private val _uiState = MutableStateFlow<List<Message>>(
        // 静态模拟数据（第一周用，后续替换为真实数据）
        listOf(
            Message(role = MessageRole.AI, content = "你好！我是即梦AI，有什么可以帮你？"),
            Message(role = MessageRole.USER, content = "请问如何设计一个AI对话App？"),
            Message(role = MessageRole.AI, content = "首先需要确定核心功能，比如对话流设计、AI能力集成、本地存储等～")
        )
    )
    // uiState 是公开不可变的，仅暴露给界面层观察
    val uiState: StateFlow<List<Message>> = _uiState.asStateFlow()

    // 第一周：静态发送逻辑（仅模拟“添加一条用户消息”，后续替换为真实网络请求）
    fun sendMessage(content: String) {
        viewModelScope.launch {
            val newMessage = Message(role = MessageRole.USER, content = content)
            // 更新消息列表（添加新消息到原列表末尾）
            _uiState.value = _uiState.value + newMessage
        }
    }
}