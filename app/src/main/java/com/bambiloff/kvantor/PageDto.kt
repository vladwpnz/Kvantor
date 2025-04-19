package com.bambiloff.kvantor

data class PageDto(
    val type: String = "",
    val text: String? = null,
    val theory: String? = null,
    val question: String? = null,
    val answers: List<String>? = null,
    val correctAnswerIndex: Int? = null,
    val description: String? = null,
    val expectedCode: String? = null, // 🟢 додано
    val codeReviewPlaceholder: String? = null,
    val message: String? = null
)
