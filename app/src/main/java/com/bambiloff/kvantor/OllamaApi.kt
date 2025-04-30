package com.bambiloff.kvantor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.http.Body
import retrofit2.http.POST

/* -------- JSON-схеми ---------- */
data class CodeReviewRequest(
    val task: String,
    val code: String
)

data class CodeReviewResponse(
    val response: String
)

/* -------- Retrofit API -------- */
interface OllamaApi {

    @POST("/review")
    suspend fun reviewCode(@Body body: CodeReviewRequest): CodeReviewResponse

    companion object {
        private const val BASE_URL = "http://10.0.2.2:5000/"  // — обов’язково з кінцевим /

        fun create(): OllamaApi {
            // 1) Будуємо OkHttpClient з таймаутами
            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // таймаут на встановлення з’єднання
                .readTimeout(120, TimeUnit.SECONDS)     // таймаут на читання відповіді
                .build()

            // 2) Створюємо Retrofit, вказуємо базовий URL та наш клієнт
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OllamaApi::class.java)
        }
    }
}
