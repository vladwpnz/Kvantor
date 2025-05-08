package com.bambiloff.kvantor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bambiloff.kvantor.AiRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AiAssistantViewModel : ViewModel() {

    private val repo = AiRepository()

    val chat: StateFlow<List<com.bambiloff.kvantor.ChatMessage>> = repo.chat

    fun send(text: String) = viewModelScope.launch {
        repo.send(text)
    }
}
