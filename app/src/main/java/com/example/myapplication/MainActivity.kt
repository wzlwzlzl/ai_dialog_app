package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.database.ChatDatabase
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.ui.screen.ChatScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.ChatViewModel
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化数据库
        val chatDatabase = ChatDatabase.getDatabase(this)
        // 创建Repository实例
        val chatRepository = ChatRepository(chatDatabase.chatMessageDao())
        // 创建ViewModel
// 修改 MainActivity.kt 中的 ViewModel 创建方式
        val chatViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatViewModel(chatRepository) as T
                }
            }
        )[ChatViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(viewModel = chatViewModel)
                }
            }
        }
    }
}