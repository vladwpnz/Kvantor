package com.bambiloff.kvantor

import com.bambiloff.kvantor.ChatMessage
import com.bambiloff.kvantor.ChatApiService
import com.bambiloff.kvantor.ChatRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AiRepository {

    /* 1️⃣ lazy‑створюємо Retrofit тільки для /ask */
    private val chatApi: ChatApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ChatApiService::class.java)
    }

    /* 2️⃣ локальний state чату */
    private val _chat = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chat = _chat.asStateFlow()

    suspend fun send(prompt: String) {
        _chat.value = _chat.value + ChatMessage(ChatMessage.Role.USER, prompt)
        val answer = chatApi.ask(ChatRequest(prompt)).response
        _chat.value = _chat.value + ChatMessage(ChatMessage.Role.AI, answer)
    }
}
