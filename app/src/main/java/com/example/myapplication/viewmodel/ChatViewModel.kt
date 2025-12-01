package com.example.myapplication.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.model.ConversationSummary
import com.example.myapplication.model.Message
import com.example.myapplication.model.MessageRole
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    constructor() : this(ChatRepository())
    // 在ChatViewModel中，确认初始状态正确
    private val _uiState = MutableStateFlow<ChatUiState>(
        ChatUiState.Success(
            listOf(Message(role = MessageRole.AI, content = "你好！我是豆包AI，有什么可以帮你？"))
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // 文生文模型
    private val MODEL_NAME = "doubao-seed-1-6-lite-251015"
    // 文生图模型
    private val IMAGE_MODEL_NAME = "doubao-seedream-3-0-t2i-250415"
    private val DEFAULT_CONVERSATION_ID = "default"

    private var lastSentMessage: String? = null
    private var historyJob: Job? = null

    private val _currentConversationId = MutableStateFlow(DEFAULT_CONVERSATION_ID)
    val currentConversationId: StateFlow<String> = _currentConversationId.asStateFlow()

    private val _conversationSummaries = MutableStateFlow<List<ConversationSummary>>(emptyList())
    val conversationSummaries: StateFlow<List<ConversationSummary>> = _conversationSummaries.asStateFlow()

    // 是否处于文生图模式
    private val _isImageMode = MutableStateFlow(false)
    val isImageMode: StateFlow<Boolean> = _isImageMode.asStateFlow()

    private val conversationTitleCache = mutableMapOf<String, String>()
    private val titleMaxLength = 20

    init {
        observeConversation(DEFAULT_CONVERSATION_ID)
        observeConversationSummaries()
    }

    // 切换文生图模式
    fun toggleImageMode() {
        _isImageMode.value = !_isImageMode.value
    }

    // 加载历史消息
    private fun observeConversation(conversationId: String) {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            repository.getHistoryMessages(conversationId).collectLatest { messages ->
                if (messages.isEmpty()) {
                    showWelcomeMessage()
                } else {
                    // 如果当前状态是 Loading，不要覆盖它（保持加载状态）
                    val currentState = _uiState.value
                    if (currentState !is ChatUiState.Loading) {
                        _uiState.value = ChatUiState.Success(messages)
                    }
                }
            }
        }
    }

    private fun observeConversationSummaries() {
        viewModelScope.launch {
            repository.getConversationSummaries().collectLatest { summaries ->
                val processed = mutableListOf<ConversationSummary>()
                for (summary in summaries) {
                    val originalTitle = summary.title.orEmpty()
                    val cached = conversationTitleCache[summary.conversationId]
                    val finalTitle = when {
                        cached != null -> cached
                        originalTitle.isBlank() -> originalTitle
                        originalTitle.length <= titleMaxLength -> originalTitle
                        else -> {
                            val result = repository.summarizeText(MODEL_NAME, originalTitle)
                            val newTitle = result.getOrElse { originalTitle.take(titleMaxLength) }
                            conversationTitleCache[summary.conversationId] = newTitle
                            newTitle
                        }
                    }
                    processed += summary.copy(title = finalTitle)
                }
                _conversationSummaries.value = processed
            }
        }
    }

    // 文本对话
    fun sendMessage(content: String) {
        if (content.trim().isEmpty()) return
        lastSentMessage = content
        val conversationId = _currentConversationId.value

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

            // 保存用户消息到本地数据库
            repository.saveMessage(userMessage, conversationId)

            // 调用Repository（官方SDK）
            val result = repository.getAiResponse(
                model = MODEL_NAME,
                userMessage = content,
                conversationId = conversationId
            )

            if (result.isSuccess) {
                // 成功：添加AI回复
                val aiMessage = Message(
                    role = MessageRole.AI,
                    content = result.getOrThrow()
                )
                // 保存AI消息到本地数据库
                repository.saveMessage(aiMessage, conversationId)
                _uiState.value = ChatUiState.Success(newMessages + aiMessage)
            } else {
                // 失败：显示错误
                val errorMsg = result.exceptionOrNull()?.message ?: "调用失败"
                updateErrorState(newMessages, errorMsg)
                Log.e("ChatViewModel", "AI调用失败", result.exceptionOrNull())
            }
        }
    }

    // 文生图：使用图片模型生成图片并保存到本地
    fun sendImage(prompt: String, context: Context) {
        if (prompt.trim().isEmpty()) return
        lastSentMessage = prompt
        val conversationId = _currentConversationId.value

        viewModelScope.launch {
            val currentState = _uiState.value
            val currentMessages = when (currentState) {
                is ChatUiState.Success -> currentState.messages
                is ChatUiState.Loading -> currentState.messages
                is ChatUiState.Error -> currentState.messages
            }

            // 添加用户消息并进入加载状态
            val userMessage = Message(role = MessageRole.USER, content = prompt)
            val newMessages = currentMessages + userMessage
            _uiState.value = ChatUiState.Loading(newMessages)

            // 保存用户消息到本地数据库
            repository.saveMessage(userMessage, conversationId)

            val result = repository.generateImage(
                model = IMAGE_MODEL_NAME,
                prompt = prompt,
                context = context
            )

            if (result.isSuccess) {
                val savedUri = result.getOrThrow()
                // 这里直接把图片的 Uri 字符串作为消息内容，UI 里根据 content:// 识别并显示图片
                val aiMessage = Message(
                    role = MessageRole.AI,
                    content = savedUri
                )
                repository.saveMessage(aiMessage, conversationId)
                _uiState.value = ChatUiState.Success(newMessages + aiMessage)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "调用失败"
                updateErrorState(newMessages, errorMsg)
                Log.e("ChatViewModel", "生成图片失败", result.exceptionOrNull())
            }
        }
    }

    fun retryLastMessage() {
        lastSentMessage?.let { sendMessage(it) }
    }

    // 清除当前对话
    fun clearCurrentConversation() {
        viewModelScope.launch {
            repository.clearConversation(_currentConversationId.value)
            // 显示欢迎消息
            showWelcomeMessage()
        }
    }

    fun selectConversation(conversationId: String) {
        if (conversationId == _currentConversationId.value) return
        _currentConversationId.value = conversationId
        observeConversation(conversationId)
    }

    fun startNewConversation() {
        val newId = "conv-${System.currentTimeMillis()}"
        _currentConversationId.value = newId
        observeConversation(newId)
    }

    private fun updateErrorState(currentMessages: List<Message>, errorMsg: String) {
        val errorMessage = Message(
            role = MessageRole.AI,
            content = "出错了，请检查网络"
        )
        _uiState.value = ChatUiState.Error(
            messages = currentMessages + errorMessage,
            errorMessage = errorMsg
        )
    }

    private fun showWelcomeMessage() {
        val welcomeMessage = Message(
            role = MessageRole.AI,
            content = "你好！我是豆包AI，有什么可以帮你？"
        )
        _uiState.value = ChatUiState.Success(listOf(welcomeMessage))
    }

    // 页面销毁时关闭服务
    override fun onCleared() {
        super.onCleared()
        repository.shutdown()
    }
}