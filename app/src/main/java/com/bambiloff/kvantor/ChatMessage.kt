package com.bambiloff.kvantor

data class ChatMessage(
    val role: Role,
    val text: String
) {
    enum class Role { USER, AI }
}
