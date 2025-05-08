package com.bambiloff.kvantor

import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApiService {
    @POST("/ask")
    suspend fun ask(@Body req: ChatRequest): ChatResponse
}

data class ChatRequest(val prompt: String)
data class ChatResponse(val response: String)
