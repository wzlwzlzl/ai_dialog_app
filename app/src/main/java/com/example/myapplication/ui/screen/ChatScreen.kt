//package com.example.myapplication.ui.screen
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Send
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.ExperimentalComposeUiApi
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import com.example.myapplication.model.Message
//import com.example.myapplication.model.MessageRole
//import com.example.myapplication.ui.component.ChatBubble
//import com.example.myapplication.viewmodel.ChatUiState
//import com.example.myapplication.viewmodel.ChatViewModel
//
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
//@Composable
//fun ChatScreen(viewModel: ChatViewModel) {
//    // 监听来自 ViewModel 的 UI 状态
//    val uiState by viewModel.uiState.collectAsState()
//    var inputText by remember { mutableStateOf("") }
//    val listState = rememberLazyListState()
//    val focusManager = LocalFocusManager.current
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    // 从不同的 UI 状态中提取消息列表
//    val messageList = when (uiState) {
//        is ChatUiState.Success -> (uiState as ChatUiState.Success).messages
//        is ChatUiState.Loading -> (uiState as ChatUiState.Loading).messages
//        is ChatUiState.Error -> (uiState as ChatUiState.Error).messages
//    }
//
//    // 当消息列表或状态变化时，自动滚动到底部
//    LaunchedEffect(uiState) {
//        if (messageList.isNotEmpty()) {
//            listState.scrollToItem(messageList.lastIndex)
//        }
//    }
//
//    // 隐藏键盘的函数
//    val hideKeyboard = {
//        focusManager.clearFocus()
//        keyboardController?.hide()
//    }
//
//    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = { Text(text = "即梦AI对话") },
//                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer
//                )
//            )
//        },
//        bottomBar = {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp)
//                    .background(MaterialTheme.colorScheme.surface)
//                    .border(
//                        width = 1.dp,
//                        color = MaterialTheme.colorScheme.outline,
//                        shape = MaterialTheme.shapes.small
//                    )
//                    .padding(4.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                OutlinedTextField(
//                    modifier = Modifier
//                        .weight(1f)
//                        .padding(horizontal = 4.dp),
//                    value = inputText,
//                    onValueChange = { inputText = it },
//                    placeholder = { Text(text = "输入消息...") },
//                    maxLines = 3,
//                    singleLine = false,
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedBorderColor = MaterialTheme.colorScheme.primary,
//                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
//                    ),
//                    shape = MaterialTheme.shapes.small,
//                    // 在加载状态时禁用输入框
//                    enabled = uiState !is ChatUiState.Loading
//                )
//
//                IconButton(
//                    onClick = {
//                        if (inputText.isNotBlank() && uiState !is ChatUiState.Loading) {
//                            viewModel.sendMessage(inputText)
//                            inputText = ""
//                            hideKeyboard()
//                        }
//                    },
//                    // 只有在输入框非空且不在加载状态时才启用发送按钮
//                    enabled = inputText.isNotBlank() && uiState !is ChatUiState.Loading
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Send,
//                        contentDescription = "发送消息",
//                        tint = if (inputText.isNotBlank() && uiState !is ChatUiState.Loading)
//                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
//                    )
//                }
//            }
//        },
//        modifier = Modifier.fillMaxSize()
//    ) { innerPadding ->
//        // 点击空白区域隐藏键盘
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .clickable { hideKeyboard() }
//        ) {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding),
//                state = listState,
//                contentPadding = PaddingValues(bottom = 16.dp)
//            ) {
//                // 显示消息列表
//                items(messageList) { message ->
//                    ChatBubble(message = message)
//                }
//
//                // 如果是加载状态，显示 "AI正在输入..."
//                if (uiState is ChatUiState.Loading) {
//                    item {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp),
//                            horizontalArrangement = Arrangement.Start
//                        ) {
//                            ChatBubble(
//                                message = Message(
//                                    role = MessageRole.AI,
//                                    content = "AI正在输入..."
//                                )
//                            )
//                        }
//                    }
//                }
//
//                // 如果是错误状态，显示错误信息和重试按钮
//                if (uiState is ChatUiState.Error) {
//                    item {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(8.dp),
//                            horizontalArrangement = Arrangement.Center,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = (uiState as ChatUiState.Error).errorMessage,
//                                color = MaterialTheme.colorScheme.error,
//                                modifier = Modifier.padding(end = 8.dp)
//                            )
//                            TextButton(
//                                onClick = { viewModel.retryLastMessage() },
//                                colors = ButtonDefaults.textButtonColors(
//                                    contentColor = MaterialTheme.colorScheme.primary
//                                )
//                            ) {
//                                Text("重试")
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true, device = "id:pixel_5")
//@Composable
//fun ChatScreenPreview() {
//    // 预览用的 ViewModel，可以提供一些模拟数据
//    val previewViewModel = ChatViewModel()
//    MaterialTheme {
//        ChatScreen(viewModel = previewViewModel)
//    }
//}
package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    // 监听来自 ViewModel 的 UI 状态
    val uiState by viewModel.uiState.collectAsState()
    val conversationSummaries by viewModel.conversationSummaries.collectAsState()
    val currentConversationId by viewModel.currentConversationId.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val dateFormatter = remember {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
    }

    // 从不同的 UI 状态中提取消息列表
    val messageList = when (uiState) {
        is ChatUiState.Success -> (uiState as ChatUiState.Success).messages
        is ChatUiState.Loading -> (uiState as ChatUiState.Loading).messages
        is ChatUiState.Error -> (uiState as ChatUiState.Error).messages
    }

    // 当消息列表或状态变化时，自动滚动到底部
    LaunchedEffect(uiState) {
        if (messageList.isNotEmpty()) {
            listState.scrollToItem(messageList.lastIndex)
        }
    }

    // 隐藏键盘的函数
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
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = "即梦AI对话") },
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
                        IconButton(onClick = { viewModel.clearCurrentConversation() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "清除对话"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        placeholder = { Text(text = "输入消息...") },
                        maxLines = 3,
                        singleLine = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = MaterialTheme.shapes.small,
                        // 在加载状态时禁用输入框
                        enabled = uiState !is ChatUiState.Loading
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && uiState !is ChatUiState.Loading) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                                hideKeyboard()
                            }
                        },
                        // 只有在输入框非空且不在加载状态时才启用发送按钮
                        enabled = inputText.isNotBlank() && uiState !is ChatUiState.Loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "发送消息",
                            tint = if (inputText.isNotBlank() && uiState !is ChatUiState.Loading)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            // 点击空白区域隐藏键盘
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { hideKeyboard() }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    // 显示消息列表
                    items(messageList) { message ->
                        ChatBubble(message = message)
                    }

                    // 如果是加载状态，显示 "AI正在输入..."
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
                                        content = "AI正在输入..."
                                    )
                                )
                            }
                        }
                    }

                    // 如果是错误状态，显示错误信息和重试按钮
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
        }
    }
}
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun ChatScreenPreview() {
    val previewRepository = ChatRepository() // 现在可以无参构造了
    val previewViewModel = ChatViewModel(previewRepository)
    MaterialTheme {
        ChatScreen(viewModel = previewViewModel)
    }
}

//@Preview(showBackground = true, device = "id:pixel_5")
//@Composable
//fun ChatScreenPreview() {
//    // 预览用的 ViewModel，可以提供一些模拟数据
//    val previewViewModel = ChatViewModel()
//    MaterialTheme {
//        ChatScreen(viewModel = previewViewModel)
//    }
//}