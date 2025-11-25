package com.example.myapplication.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ArkApiClient {
    // 火山引擎API基础地址
    private const val BASE_URL = "https://ark.cn-beijing.volces.com/"

    val apiService: ArkApiService by lazy {
        // 日志拦截器（调试用）
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArkApiService::class.java)
    }
}