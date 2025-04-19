package com.bambiloff.kvantor

data class ModuleDto(
    var id: String = "",
    var title: String = "",
    var pages: List<PageDto> = emptyList()
) {
    fun toModule(): Module {
        return Module(
            id = id,
            title = title,
            pages = pages.mapNotNull { it.toPageOrNull() }
        )
    }
}

fun PageDto.toPageOrNull(): Page? {
    return try {
        when (type.lowercase()) {
            "theory" -> Page.Theory(text ?: theory ?: "")
            "test" -> Page.Test(
                question = question ?: "",
                answers = answers ?: emptyList(),
                correctAnswerIndex = correctAnswerIndex ?: 0
            )
            "coding" -> Page.CodingTask(
                description = description ?: "",
                expectedCode = expectedCode ?: "",
                codeReviewPlaceholder = codeReviewPlaceholder ?: ""
            )
            "final" -> Page.Final(message ?: "🎉 Вітаємо! Ви завершили модуль.")
            else -> null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
