package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 颜色定义
private val Purple500 = Color(0xFF6200EE)
private val Purple80 = Color(0xFFD0BCFF)
private val PurpleGrey40 = Color(0xFF625b71)
private val Pink40 = Color(0xFFEF9A9A)

// 深色配色方案
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey40,
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

// 浅色配色方案
private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // 直接使用 Material3 内置的 Typography
        content = content
    )
}