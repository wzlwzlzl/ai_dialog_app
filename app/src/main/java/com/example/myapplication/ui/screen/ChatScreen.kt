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
//import android.widget.Toast
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import com.example.myapplication.model.Message
//import com.example.myapplication.model.MessageRole
//import com.example.myapplication.ui.component.ChatBubble
//import com.example.myapplication.viewmodel.ChatViewModel
//
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
//@Composable
//fun ChatScreen(viewModel: ChatViewModel) {
//    val messageList by viewModel.uiState.collectAsState()
//    var inputText by remember { mutableStateOf("") }
//    val listState = rememberLazyListState()
//    val context = LocalContext.current
//    // 获取焦点管理器和键盘控制器
//    val focusManager = LocalFocusManager.current
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    LaunchedEffect(messageList) {
//        if (messageList.isNotEmpty()) {
//            listState.scrollToItem(messageList.lastIndex)
//        }
//    }
//
//    // 点击空白处隐藏键盘的处理函数
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
//                    shape = MaterialTheme.shapes.small
//                )
//
//                IconButton(
//                    onClick = {
//                        if (inputText.isNotBlank()) {
//                            viewModel.sendMessage(inputText)
//                            inputText = ""
//                            // 发送消息后隐藏键盘
//                            hideKeyboard()
//                            Toast.makeText(context, "静态消息已发送（后续会对接AI）", Toast.LENGTH_SHORT).show()
//                        }
//                    },
//                    enabled = inputText.isNotBlank()
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Send,
//                        contentDescription = "发送消息",
//                        tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
//                    )
//                }
//            }
//        },
//        modifier = Modifier.fillMaxSize()
//    ) { innerPadding ->
//        // 在消息列表区域添加点击事件，点击时隐藏键盘
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
//                items(messageList.size) { index ->
//                    ChatBubble(message = messageList[index])
//                }
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true, device = "id:pixel_5")
//@Composable
//fun ChatScreenPreview() {
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.myapplication.model.Message
import com.example.myapplication.model.MessageRole
import com.example.myapplication.ui.component.ChatBubble
import com.example.myapplication.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messageList by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    // 获取焦点管理器和键盘控制器
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(messageList) {
        if (messageList.isNotEmpty()) {
            listState.scrollToItem(messageList.lastIndex)
        }
    }

    // 点击空白处隐藏键盘的处理函数
    val hideKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "即梦AI对话") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
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
                    shape = MaterialTheme.shapes.small
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                            // 发送消息后隐藏键盘
                            hideKeyboard()
                            Toast.makeText(context, "静态消息已发送（后续会对接AI）", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = inputText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送消息",
                        tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        // 在消息列表区域添加点击事件，点击时隐藏键盘
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
                items(messageList.size) { index ->
                    ChatBubble(message = messageList[index])
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun ChatScreenPreview() {
    val previewViewModel = ChatViewModel()
    MaterialTheme {
        ChatScreen(viewModel = previewViewModel)
    }
}