package com.example.myapplication.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Message
import com.example.myapplication.model.MessageRole

// 聊天气泡组件（接收单个 Message 对象，自动区分样式）
@Composable
fun ChatBubble(message: Message) {
    // 按角色区分布局方向：用户消息靠右，AI消息靠左
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (message.role == MessageRole.USER) {
            Arrangement.End  // 用户消息靠右
        } else {
            Arrangement.Start  // AI消息靠左
        }
    ) {
        // 气泡内容
        Box(
            modifier = Modifier
                .background(
                    color = if (message.role == MessageRole.USER) {
                        Color(0xFF6200EE)  // 用户气泡：Material 3 主题蓝
                    } else {
                        Color(0xFFF5F5F5)  // AI气泡：浅灰色
                    },
                    shape = RoundedCornerShape(16.dp)
                        .copy(  // 气泡圆角优化：避免角色侧的圆角过大
                            topStart = CornerSize(16.dp),
                            topEnd = CornerSize(16.dp),
                            bottomStart = if (message.role == MessageRole.USER) CornerSize(16.dp) else CornerSize(4.dp),
                            bottomEnd = if (message.role == MessageRole.USER) CornerSize(4.dp) else CornerSize(16.dp)
                        )
                )
                .border(
                    width = 1.dp,
                    color = if (message.role == MessageRole.USER) Color(0xFF6200EE) else Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
                .widthIn(max = 280.dp)  // 限制气泡最大宽度，避免文字过长
        ) {
            Text(
                text = message.content,
                color = if (message.role == MessageRole.USER) Color.White else Color.Black,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}