package com.flasska.chatai.presentation.chat

import com.flasska.chatai.domain.model.Message

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isProcessing: Boolean = false // Флаг обработки сообщения AI
) {
    // Проверяем, ожидается ли ответ от AI (последнее сообщение от пользователя)
    val isWaitingForResponse: Boolean
        get() = messages.isNotEmpty() && messages.last().isFromUser && isProcessing
}

