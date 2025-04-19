package com.bambiloff.kvantor

sealed class Page {
    data class Theory(val text: String) : Page()
    data class Test(
        val question: String,
        val answers: List<String>,
        val correctAnswerIndex: Int
    ) : Page()
    data class CodingTask(
        val description: String,
        val expectedCode: String,
        val codeReviewPlaceholder: String
    ) : Page()
    data class Final(val message: String) : Page()
}
