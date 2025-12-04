# 即梦AI对话 - 对话与图片生成工具

这是一个基于Android平台的AI应用，集成了文本对话和文生图功能，采用Jetpack Compose构建UI，遵循MVVM架构模式，支持本地对话记录管理。


## 功能概述

- **AI对话**：与AI进行自然语言交互，支持上下文对话
- **文生图**：切换至图片模式，输入提示词生成图片并保存至系统相册
- **对话管理**：支持新建、切换、清除历史对话，对话列表展示最近更新时间
- **本地存储**：所有对话记录持久化存储在本地数据库


## 技术栈

- **UI框架**：Jetpack Compose
- **架构组件**：ViewModel、StateFlow、Coroutines
- **本地存储**：Room Database
- **网络请求**：Retrofit、OkHttp
- **AI服务**：Volcengine Ark SDK
- **图片加载**：Coil
- **构建工具**：Gradle Kotlin DSL


## 运行说明

### 环境要求

- Android Studio 2023.1+
- Kotlin 2.0.21+
- Gradle 8.13+
- Android SDK 36+ (Android 8.0+)


### 配置步骤

1. 克隆项目到本地
2. 打开Android Studio，导入项目
3. 替换AI服务API Key（可选）：
   在`ChatRepository.kt`中，修改`arkService`初始化的`apiKey`为你的Volcengine API Key：
   ```kotlin
   private val arkService by lazy {
       ArkService.builder()
           .apiKey("your API Key") // 替换为实际API Key
           .baseUrl("https://ark.cn-beijing.volces.com/api/v3")
           .build()
   }
   ```
4. 同步Gradle依赖
5. 运行应用到模拟器或实体设备


## 核心功能实现说明

### 1. 对话流程

1. 用户输入消息后，`ChatViewModel.sendMessage()`被调用
2. 本地先添加用户消息并进入加载状态
3. 调用`ChatRepository.getAiResponse()`获取AI回复（包含历史上下文）
4. 收到回复后更新UI状态，并将AI消息保存到本地数据库


### 2. 文生图功能

1. 通过顶部按钮切换至图片模式（`isImageMode`状态控制）
2. 输入提示词后，调用`ChatRepository.generateImage()`
3. 生成成功后，图片会保存到系统相册，并以`content://` URI形式展示在对话中


### 3. 对话管理

- 左侧抽屉展示所有历史对话摘要（`ConversationSummary`）
- 支持新建对话（`startNewConversation()`）
- 支持切换对话（`selectConversation()`）
- 支持清除当前对话（`clearCurrentConversation()`）


## 注意事项

- 确保设备联网（应用需要访问AI服务）
- 图片生成功能需要存储权限（自动通过MediaStore申请，无需额外权限声明）
- 历史对话存储在本地SQLite数据库中，卸载应用会丢失数据


## 依赖说明

主要依赖版本可在`gradle/libs.versions.toml`中查看，核心依赖包括：

- Compose相关：`2024.09.00`
- Lifecycle组件：`2.6.2`
- Room数据库：`2.6.1`
- Volcengine Ark SDK：`0.2.47`
- Retrofit：`2.9.0`
- Coil：`2.6.0`
