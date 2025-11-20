package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.ui.screen.ChatScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.ChatViewModel
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
//import androidx.compose.ui.fillMaxsize // 必须导入的关键依赖
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        setContent { // setContent 是 Compose 上下文的合法入口
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), // 在此处调用 fillMaxSize
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(viewModel = chatViewModel)
                }
            }
        }
    }
}