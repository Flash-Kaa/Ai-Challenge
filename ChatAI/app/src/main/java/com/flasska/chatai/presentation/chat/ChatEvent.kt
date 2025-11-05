package com.flasska.chatai.presentation.chat

sealed class ChatEvent {
    data class SendMessage(val text: String) : ChatEvent()
}
