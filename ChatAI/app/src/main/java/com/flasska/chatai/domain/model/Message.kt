package com.flasska.chatai.domain.model

import java.util.UUID

data class Message(
    val text: String,
    val isFromUser: Boolean,
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis()
)

