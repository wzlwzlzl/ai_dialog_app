package com.example.myapplication.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.model.Message
import com.example.myapplication.model.MessageRole
import com.example.myapplication.ui.component.ChatBubble
import com.example.myapplication.viewmodel.ChatUiState
import com.example.myapplication.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val conversationSummaries by viewModel.conversationSummaries.collectAsState()
    val currentConversationId by viewModel.currentConversationId.collectAsState()
    val isImageMode by viewModel.isImageMode.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        drawerState.open()
    }

    val dateFormatter = remember {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    }

    val messageList = when (uiState) {
        is ChatUiState.Success -> (uiState as ChatUiState.Success).messages
        is ChatUiState.Loading -> (uiState as ChatUiState.Loading).messages
        is ChatUiState.Error -> (uiState as ChatUiState.Error).messages
    }

    LaunchedEffect(uiState) {
        if (messageList.isNotEmpty()) {
            listState.scrollToItem(messageList.lastIndex)
        }
    }

    val hideKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "历史对话",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                TextButton(
                    onClick = {
                        viewModel.startNewConversation()
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = "新建对话")
                }
                if (conversationSummaries.isEmpty()) {
                    Text(
                        text = "暂无历史对话",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                } else {
                    conversationSummaries.forEach { summary ->
                        val title = summary.title?.takeIf { it.isNotBlank() } ?: "未命名对话"
                        NavigationDrawerItem(
                            label = {
                                Column {
                                    Text(
                                        text = title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(summary.lastUpdated)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            },
                            selected = summary.conversationId == currentConversationId,
                            onClick = {
                                viewModel.selectConversation(summary.conversationId)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    ) {
        // 使用 Column 代替 Scaffold 来确保 TopAppBar 始终固定在顶部
        Column(modifier = Modifier.fillMaxSize()) {
            // 固定的 TopAppBar
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isImageMode) "即梦AI对话(图片生成)" else "即梦AI对话"
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "打开历史对话"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleImageMode() }) {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = "切换文生图模式",
                            tint = if (isImageMode) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    IconButton(onClick = { viewModel.clearCurrentConversation() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "清除对话"
                        )
                    }
                }
            )

            // 消息列表区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable { hideKeyboard() }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
                ) {
                    items(messageList) { message ->
                        ChatBubble(message = message)
                    }

                    if (uiState is ChatUiState.Loading) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                ChatBubble(
                                    message = Message(
                                        role = MessageRole.AI,
                                        content = if (isImageMode) "图片生成中..." else "AI正在输入..."
                                    )
                                )
                            }
                        }
                    }

                    if (uiState is ChatUiState.Error) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = (uiState as ChatUiState.Error).errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                TextButton(
                                    onClick = { viewModel.retryLastMessage() },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("重试")
                                }
                            }
                        }
                    }
                }
            }

            // 底部输入栏 - 紧贴键盘上方
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (isImageMode) "输入文生图提示词..." else "输入消息..."
                        )
                    },
                    maxLines = 3,
                    singleLine = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = MaterialTheme.shapes.small,
                    enabled = uiState !is ChatUiState.Loading
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && uiState !is ChatUiState.Loading) {
                            if (isImageMode) {
                                viewModel.sendImage(inputText, context)
                            } else {
                                viewModel.sendMessage(inputText)
                            }
                            inputText = ""
                            hideKeyboard()
                        }
                    },
                    enabled = inputText.isNotBlank() && uiState !is ChatUiState.Loading
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = if (isImageMode) "生成图片" else "发送消息",
                        tint = if (inputText.isNotBlank() && uiState !is ChatUiState.Loading)
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun ChatScreenPreview() {
    val previewRepository = ChatRepository()
    val previewViewModel = ChatViewModel(previewRepository)
    MaterialTheme {
        ChatScreen(viewModel = previewViewModel)
    }
}
